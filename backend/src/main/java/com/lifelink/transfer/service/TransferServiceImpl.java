package com.lifelink.transfer.service;

import com.lifelink.audit.service.AuditService;
import com.lifelink.common.exception.BusinessRuleException;
import com.lifelink.common.exception.ResourceNotFoundException;
import com.lifelink.common.exception.UnauthorizedFacilityAccessException;
import com.lifelink.facility.model.Facility;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.InventoryTransaction;
import com.lifelink.inventory.model.enums.TransactionType;
import com.lifelink.inventory.repository.BloodInventoryBatchRepository;
import com.lifelink.inventory.repository.InventoryTransactionRepository;
import com.lifelink.notification.model.enums.NotificationType;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.request.model.BloodRequest;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.repository.BloodRequestRepository;
import com.lifelink.request.statemachine.RequestStateValidator;
import com.lifelink.reservation.model.StockReservation;
import com.lifelink.reservation.model.enums.ReservationStatus;
import com.lifelink.reservation.repository.StockReservationRepository;
import com.lifelink.security.SecurityUtils;
import com.lifelink.transfer.dto.ConfirmReceiptRequest;
import com.lifelink.transfer.dto.DispatchTransferRequest;
import com.lifelink.transfer.dto.TransferDto;
import com.lifelink.transfer.model.BloodTransfer;
import com.lifelink.transfer.model.enums.TransferStatus;
import com.lifelink.transfer.repository.BloodTransferRepository;
import com.lifelink.transfer.statemachine.TransferStateValidator;
import com.lifelink.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    private final BloodTransferRepository transferRepository;
    private final StockReservationRepository reservationRepository;
    private final BloodInventoryBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final BloodRequestRepository requestRepository;
    private final TransferStateValidator transferStateValidator;
    private final RequestStateValidator requestStateValidator;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public TransferServiceImpl(BloodTransferRepository transferRepository,
                               StockReservationRepository reservationRepository,
                               BloodInventoryBatchRepository batchRepository,
                               InventoryTransactionRepository transactionRepository,
                               BloodRequestRepository requestRepository,
                               TransferStateValidator transferStateValidator,
                               RequestStateValidator requestStateValidator,
                               NotificationService notificationService,
                               AuditService auditService,
                               SecurityUtils securityUtils) {
        this.transferRepository = transferRepository;
        this.reservationRepository = reservationRepository;
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.requestRepository = requestRepository;
        this.transferStateValidator = transferStateValidator;
        this.requestStateValidator = requestStateValidator;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional
    public TransferDto dispatchTransfer(DispatchTransferRequest req) {
        User dispatcher = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BusinessRuleException("User must be authenticated."));

        StockReservation reservation = reservationRepository.findById(req.reservationId())
                .orElseThrow(() -> new ResourceNotFoundException("StockReservation", req.reservationId()));

        if (reservation.getStatus() != ReservationStatus.ACTIVE && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Cannot dispatch an inactive or already released reservation: " + reservation.getStatus());
        }

        Facility sourceBank = reservation.getBloodBank();
        if (!dispatcher.isNetworkAdmin() && (dispatcher.getFacility() == null || !dispatcher.getFacility().getId().equals(sourceBank.getId()))) {
            throw new UnauthorizedFacilityAccessException(dispatcher.getId(), sourceBank.getId());
        }

        BloodRequest request = reservation.getRequest();
        Facility destinationHospital = request.getHospital();

        // Lock and update inventory batch atomically
        BloodInventoryBatch batch = batchRepository.findByIdForUpdate(reservation.getBatch().getId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodInventoryBatch", reservation.getBatch().getId()));

        batch.dispatch(reservation.getUnitsReserved());
        batchRepository.save(batch);

        reservation.markDispatched();
        reservationRepository.save(reservation);

        // Create Transfer record
        String transferRef = "TRF-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        BloodTransfer transfer = new BloodTransfer(
                transferRef,
                request,
                reservation,
                sourceBank,
                destinationHospital,
                reservation.getUnitsReserved()
        );

        transfer.dispatch(dispatcher, req.courierDetails(), req.temperatureLogSummary(), req.notes());
        BloodTransfer savedTransfer = transferRepository.save(transfer);

        // Update request status to IN_TRANSFER if not already
        if (requestStateValidator.canTransition(request.getStatus(), RequestStatus.IN_TRANSFER)) {
            request.transitionStatus(RequestStatus.IN_TRANSFER);
            requestRepository.save(request);
        }

        // Log authoritative dispatch inventory transaction
        InventoryTransaction tx = new InventoryTransaction(
                batch,
                sourceBank,
                TransactionType.DISPATCH,
                -reservation.getUnitsReserved(),
                batch.getAvailableQuantity(),
                "BLOOD_TRANSFER_DISPATCH",
                savedTransfer.getId(),
                dispatcher,
                "Dispatched to " + destinationHospital.getName() + " (Courier: " + req.courierDetails() + ")"
        );
        transactionRepository.save(tx);

        // Notify Hospital
        notificationService.notifyFacility(destinationHospital,
                "Blood Transfer Dispatched",
                String.format("%s has dispatched %d units of %s. Ref: %s.",
                        sourceBank.getName(), reservation.getUnitsReserved(), batch.getBloodGroup().getDisplayName(), transferRef),
                NotificationType.INFO, "/transfers/" + savedTransfer.getId());

        auditService.record(dispatcher.getId(), dispatcher.getEmail(), dispatcher.getRole().name(),
                sourceBank.getId(), "DISPATCH_TRANSFER", "BloodTransfer", savedTransfer.getId(),
                "Dispatched " + reservation.getUnitsReserved() + " units to " + destinationHospital.getName(), null);

        return TransferDto.fromEntity(savedTransfer);
    }

    @Override
    @Transactional
    public TransferDto confirmReceipt(Long transferId, ConfirmReceiptRequest req) {
        User receiver = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BusinessRuleException("User must be authenticated."));

        BloodTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodTransfer", transferId));

        Facility destinationHospital = transfer.getDestinationFacility();
        if (!receiver.isNetworkAdmin() && (receiver.getFacility() == null || !receiver.getFacility().getId().equals(destinationHospital.getId()))) {
            throw new UnauthorizedFacilityAccessException(receiver.getId(), destinationHospital.getId());
        }

        transferStateValidator.validateTransition(transfer.getStatus(), TransferStatus.RECEIVED);
        transfer.confirmReceipt(receiver, req.notes());
        BloodTransfer savedTransfer = transferRepository.save(transfer);

        // Update Request status to FULFILLED
        BloodRequest request = transfer.getRequest();
        if (requestStateValidator.canTransition(request.getStatus(), RequestStatus.FULFILLED)) {
            request.transitionStatus(RequestStatus.FULFILLED);
            requestRepository.save(request);
        }

        // Notify source blood bank
        notificationService.notifyFacility(transfer.getSourceFacility(),
                "Blood Transfer Delivered & Confirmed",
                String.format("%s confirmed safe receipt of %d units for request %s.",
                        destinationHospital.getName(), transfer.getUnitsTransferred(), request.getRequestReference()),
                NotificationType.CONFIRMATION, "/transfers/" + transferId);

        auditService.record(receiver.getId(), receiver.getEmail(), receiver.getRole().name(),
                destinationHospital.getId(), "CONFIRM_RECEIPT", "BloodTransfer", transferId,
                "Confirmed receipt of transfer " + transfer.getTransferReference(), null);

        return TransferDto.fromEntity(savedTransfer);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferDto getTransferById(Long transferId) {
        BloodTransfer t = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodTransfer", transferId));
        return TransferDto.fromEntity(t);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getSourceTransfers(Long facilityId, Pageable pageable) {
        return transferRepository.findBySourceFacilityIdOrderByCreatedAtDesc(facilityId, pageable)
                .map(TransferDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getDestinationTransfers(Long facilityId, Pageable pageable) {
        return transferRepository.findByDestinationFacilityIdOrderByCreatedAtDesc(facilityId, pageable)
                .map(TransferDto::fromEntity);
    }
}
