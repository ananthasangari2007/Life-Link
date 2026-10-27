package com.lifelink.reservation.service;

import com.lifelink.audit.service.AuditService;
import com.lifelink.common.exception.BusinessRuleException;
import com.lifelink.common.exception.ResourceNotFoundException;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.InventoryTransaction;
import com.lifelink.inventory.model.enums.TransactionType;
import com.lifelink.inventory.repository.BloodInventoryBatchRepository;
import com.lifelink.inventory.repository.InventoryTransactionRepository;
import com.lifelink.request.model.BloodRequest;
import com.lifelink.reservation.dto.ReservationDto;
import com.lifelink.reservation.model.StockReservation;
import com.lifelink.reservation.model.enums.ReservationStatus;
import com.lifelink.reservation.repository.StockReservationRepository;
import com.lifelink.security.SecurityUtils;
import com.lifelink.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final StockReservationRepository reservationRepository;
    private final BloodInventoryBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final int reservationExpiryHours;

    public ReservationServiceImpl(StockReservationRepository reservationRepository,
                                  BloodInventoryBatchRepository batchRepository,
                                  InventoryTransactionRepository transactionRepository,
                                  AuditService auditService,
                                  SecurityUtils securityUtils,
                                  @Value("${app.coordination.reservation-expiry-hours:24}") int reservationExpiryHours) {
        this.reservationRepository = reservationRepository;
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.reservationExpiryHours = reservationExpiryHours;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StockReservation createReservation(BloodRequest request, BloodInventoryBatch batchRef, int units) {
        // Concurrency Control: Acquire Pessimistic Write Lock on the batch entity
        BloodInventoryBatch lockedBatch = batchRepository.findByIdForUpdate(batchRef.getId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodInventoryBatch", batchRef.getId()));

        // Domain method validates available stock and atomically increments reservedQuantity
        lockedBatch.reserve(units);
        batchRepository.save(lockedBatch);

        String refCode = "RES-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Instant expiresAt = Instant.now().plus(reservationExpiryHours, ChronoUnit.HOURS);

        StockReservation reservation = new StockReservation(
                refCode,
                request,
                lockedBatch,
                lockedBatch.getFacility(),
                units,
                expiresAt
        );

        StockReservation saved = reservationRepository.save(reservation);

        User currentUser = securityUtils.getCurrentUser().orElse(null);
        // Log authoritative inventory transaction
        InventoryTransaction tx = new InventoryTransaction(
                lockedBatch,
                lockedBatch.getFacility(),
                TransactionType.ALLOCATION,
                -units,
                lockedBatch.getAvailableQuantity(),
                "STOCK_RESERVATION",
                saved.getId(),
                currentUser,
                "Reserved " + units + " units for request " + request.getRequestReference()
        );
        transactionRepository.save(tx);

        auditService.record(
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : "system",
                currentUser != null ? currentUser.getRole().name() : "SYSTEM",
                lockedBatch.getFacility().getId(),
                "CREATE_RESERVATION",
                "StockReservation",
                saved.getId(),
                String.format("Reserved %d units from batch %s for request %s (Ref: %s)",
                        units, lockedBatch.getBatchNumber(), request.getRequestReference(), refCode),
                null
        );

        return saved;
    }

    @Override
    @Transactional
    public ReservationDto releaseReservation(Long reservationId, String reason) {
        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("StockReservation", reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Cannot release reservation in status: " + reservation.getStatus(), "INVALID_RESERVATION_STATUS");
        }

        // Lock batch to safely restore stock
        BloodInventoryBatch batch = batchRepository.findByIdForUpdate(reservation.getBatch().getId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodInventoryBatch", reservation.getBatch().getId()));

        batch.releaseReservation(reservation.getUnitsReserved());
        batchRepository.save(batch);

        reservation.release();
        StockReservation savedReservation = reservationRepository.save(reservation);

        User currentUser = securityUtils.getCurrentUser().orElse(null);
        InventoryTransaction tx = new InventoryTransaction(
                batch,
                batch.getFacility(),
                TransactionType.RELEASE,
                reservation.getUnitsReserved(),
                batch.getAvailableQuantity(),
                "RESERVATION_RELEASE",
                reservation.getId(),
                currentUser,
                "Released reservation: " + (reason != null ? reason : "Manual release")
        );
        transactionRepository.save(tx);

        auditService.record(
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : "system",
                currentUser != null ? currentUser.getRole().name() : "SYSTEM",
                batch.getFacility().getId(),
                "RELEASE_RESERVATION",
                "StockReservation",
                reservation.getId(),
                "Released " + reservation.getUnitsReserved() + " units back to available stock. Reason: " + reason,
                null
        );

        return ReservationDto.fromEntity(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDto> getRequestReservations(Long requestId) {
        return reservationRepository.findByRequestId(requestId)
                .stream()
                .map(ReservationDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationDto> getBloodBankReservations(Long bloodBankId, Pageable pageable) {
        return reservationRepository.findByBloodBankIdOrderByCreatedAtDesc(bloodBankId, pageable)
                .map(ReservationDto::fromEntity);
    }

    @Override
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void releaseExpiredReservations() {
        List<StockReservation> expired = reservationRepository.findExpiredActiveReservations(Instant.now());
        for (StockReservation r : expired) {
            try {
                log.info("Expiring inactive reservation [ID: {} Ref: {}]", r.getId(), r.getReservationReference());
                releaseReservation(r.getId(), "Automatic expiration after threshold");
                r.markExpired();
                reservationRepository.save(r);
            } catch (Exception ex) {
                log.error("Failed to release expired reservation {}: {}", r.getId(), ex.getMessage());
            }
        }
    }
}
