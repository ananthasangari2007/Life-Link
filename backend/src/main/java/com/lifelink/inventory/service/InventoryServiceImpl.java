package com.lifelink.inventory.service;

import com.lifelink.audit.service.AuditService;
import com.lifelink.common.exception.BusinessRuleException;
import com.lifelink.common.exception.ResourceNotFoundException;
import com.lifelink.common.exception.UnauthorizedFacilityAccessException;
import com.lifelink.facility.model.Facility;
import com.lifelink.facility.repository.FacilityRepository;
import com.lifelink.inventory.dto.CreateBatchRequest;
import com.lifelink.inventory.dto.InventoryBatchDto;
import com.lifelink.inventory.dto.InventorySummaryDto;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.InventoryTransaction;
import com.lifelink.inventory.model.enums.InventoryStatus;
import com.lifelink.inventory.model.enums.TransactionType;
import com.lifelink.inventory.repository.BloodInventoryBatchRepository;
import com.lifelink.inventory.repository.InventoryTransactionRepository;
import com.lifelink.security.SecurityUtils;
import com.lifelink.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final BloodInventoryBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final FacilityRepository facilityRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    public InventoryServiceImpl(BloodInventoryBatchRepository batchRepository,
                                InventoryTransactionRepository transactionRepository,
                                FacilityRepository facilityRepository,
                                SecurityUtils securityUtils,
                                AuditService auditService) {
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.facilityRepository = facilityRepository;
        this.securityUtils = securityUtils;
        this.auditService = auditService;
    }

    private void verifyFacilityAccess(Long facilityId) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BusinessRuleException("User not authenticated"));

        if (currentUser.isNetworkAdmin() || currentUser.isAuditor()) {
            return;
        }

        if (currentUser.getFacility() == null || !currentUser.getFacility().getId().equals(facilityId)) {
            throw new UnauthorizedFacilityAccessException(currentUser.getId(), facilityId);
        }
    }

    @Override
    @Transactional
    public InventoryBatchDto addBatch(Long facilityId, CreateBatchRequest req) {
        verifyFacilityAccess(facilityId);

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility", facilityId));

        if (!facility.isApproved()) {
            throw new BusinessRuleException("Cannot add inventory to a facility that is not yet approved.", "FACILITY_NOT_APPROVED");
        }

        if (batchRepository.findByFacilityIdAndBatchNumber(facilityId, req.batchNumber().trim().toUpperCase()).isPresent()) {
            throw new BusinessRuleException("Batch number [" + req.batchNumber() + "] already exists in your facility.", "DUPLICATE_BATCH_NUMBER");
        }

        BloodInventoryBatch batch = new BloodInventoryBatch(
                facility,
                req.batchNumber().trim().toUpperCase(),
                req.bloodGroup(),
                req.componentType(),
                req.quantity(),
                req.collectionDate() != null ? req.collectionDate() : Instant.now(),
                req.expiryDate(),
                req.storageLocation(),
                req.notes()
        );

        BloodInventoryBatch saved = batchRepository.save(batch);

        User currentUser = securityUtils.getCurrentUser().orElse(null);
        // Create authoritative audit transaction
        InventoryTransaction tx = new InventoryTransaction(
                saved,
                facility,
                TransactionType.COLLECTION,
                req.quantity(),
                saved.getAvailableQuantity(),
                "BATCH_CREATION",
                saved.getId(),
                currentUser,
                "Initial batch collection and intake"
        );
        transactionRepository.save(tx);

        auditService.record(
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : "system",
                currentUser != null ? currentUser.getRole().name() : "SYSTEM",
                facilityId,
                "ADD_INVENTORY_BATCH",
                "BloodInventoryBatch",
                saved.getId(),
                String.format("Added batch %s (%s, %s, %d units)", saved.getBatchNumber(), saved.getBloodGroup(), saved.getComponentType(), saved.getQuantity()),
                null
        );

        return InventoryBatchDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBatchDto> getFacilityBatches(Long facilityId, Pageable pageable) {
        verifyFacilityAccess(facilityId);
        return batchRepository.findByFacilityId(facilityId, pageable).map(InventoryBatchDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryBatchDto> getNearExpiryBatches(Long facilityId) {
        verifyFacilityAccess(facilityId);
        Instant now = Instant.now();
        Instant threshold = now.plus(7, ChronoUnit.DAYS);
        return batchRepository.findNearExpiryBatches(facilityId, now, threshold)
                .stream()
                .map(InventoryBatchDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventorySummaryDto getFacilitySummary(Long facilityId) {
        verifyFacilityAccess(facilityId);
        List<BloodInventoryBatch> batches = batchRepository.findByFacilityId(facilityId);

        int totalUnits = 0;
        int availableUnits = 0;
        int reservedUnits = 0;
        int dispatchedUnits = 0;
        int nearExpiryUnits = 0;
        int quarantinedUnits = 0;

        Map<String, Integer> stockByBloodGroup = new HashMap<>();
        Map<String, Integer> stockByComponent = new HashMap<>();

        Instant now = Instant.now();
        Instant nearExpiryThreshold = now.plus(7, ChronoUnit.DAYS);

        for (BloodInventoryBatch b : batches) {
            totalUnits += b.getQuantity();
            int avail = b.getAvailableQuantity();
            availableUnits += avail;
            reservedUnits += b.getReservedQuantity();
            dispatchedUnits += b.getDispatchedQuantity();

            if (b.getStatus() == InventoryStatus.QUARANTINED) {
                quarantinedUnits += (b.getQuantity() - b.getDispatchedQuantity());
            }

            if (b.getStatus() == InventoryStatus.AVAILABLE && !b.isExpired() && b.getExpiryDate().isBefore(nearExpiryThreshold)) {
                nearExpiryUnits += avail;
            }

            if (avail > 0) {
                String bg = b.getBloodGroup().getDisplayName();
                stockByBloodGroup.put(bg, stockByBloodGroup.getOrDefault(bg, 0) + avail);

                String comp = b.getComponentType().getDisplayName();
                stockByComponent.put(comp, stockByComponent.getOrDefault(comp, 0) + avail);
            }
        }

        return new InventorySummaryDto(totalUnits, availableUnits, reservedUnits, dispatchedUnits,
                nearExpiryUnits, quarantinedUnits, stockByBloodGroup, stockByComponent);
    }

    @Override
    @Transactional
    public InventoryBatchDto quarantineBatch(Long facilityId, Long batchId, String reason) {
        verifyFacilityAccess(facilityId);
        BloodInventoryBatch batch = batchRepository.findByIdForUpdate(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodInventoryBatch", batchId));

        if (!batch.getFacility().getId().equals(facilityId)) {
            throw new UnauthorizedFacilityAccessException(null, facilityId);
        }

        batch.quarantine(reason);
        BloodInventoryBatch saved = batchRepository.save(batch);

        User currentUser = securityUtils.getCurrentUser().orElse(null);
        InventoryTransaction tx = new InventoryTransaction(
                saved, batch.getFacility(), TransactionType.QUARANTINE,
                0, saved.getAvailableQuantity(), "MANUAL_QUARANTINE", saved.getId(), currentUser, reason
        );
        transactionRepository.save(tx);

        return InventoryBatchDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public InventoryBatchDto restoreBatch(Long facilityId, Long batchId) {
        verifyFacilityAccess(facilityId);
        BloodInventoryBatch batch = batchRepository.findByIdForUpdate(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodInventoryBatch", batchId));

        batch.markAvailable();
        BloodInventoryBatch saved = batchRepository.save(batch);
        return InventoryBatchDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryBatchDto getBatchById(Long batchId) {
        BloodInventoryBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodInventoryBatch", batchId));
        return InventoryBatchDto.fromEntity(batch);
    }
}
