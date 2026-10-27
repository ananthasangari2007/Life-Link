package com.lifelink.request.service;

import com.lifelink.audit.service.AuditService;
import com.lifelink.common.exception.BusinessRuleException;
import com.lifelink.common.exception.ResourceNotFoundException;
import com.lifelink.common.exception.UnauthorizedFacilityAccessException;
import com.lifelink.facility.model.Facility;
import com.lifelink.facility.model.enums.FacilityStatus;
import com.lifelink.facility.model.enums.FacilityType;
import com.lifelink.facility.repository.FacilityRepository;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.enums.InventoryStatus;
import com.lifelink.inventory.repository.BloodInventoryBatchRepository;
import com.lifelink.inventory.strategy.InventoryAllocationStrategy;
import com.lifelink.matching.policy.BloodCompatibilityPolicy;
import com.lifelink.notification.model.enums.NotificationType;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.request.dto.BankResponseActionDto;
import com.lifelink.request.dto.BloodRequestDto;
import com.lifelink.request.dto.CreateBloodRequestDto;
import com.lifelink.request.model.BloodRequest;
import com.lifelink.request.model.BloodRequestResponse;
import com.lifelink.request.model.enums.BankResponseStatus;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.repository.BloodRequestRepository;
import com.lifelink.request.repository.BloodRequestResponseRepository;
import com.lifelink.request.statemachine.RequestStateValidator;
import com.lifelink.reservation.service.ReservationService;
import com.lifelink.security.SecurityUtils;
import com.lifelink.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RequestCoordinationServiceImpl implements RequestCoordinationService {

    private final BloodRequestRepository requestRepository;
    private final BloodRequestResponseRepository responseRepository;
    private final FacilityRepository facilityRepository;
    private final BloodInventoryBatchRepository batchRepository;
    private final ReservationService reservationService;
    private final RequestStateValidator stateValidator;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final BloodCompatibilityPolicy compatibilityPolicy;
    private final InventoryAllocationStrategy allocationStrategy;

    public RequestCoordinationServiceImpl(BloodRequestRepository requestRepository,
                                         BloodRequestResponseRepository responseRepository,
                                         FacilityRepository facilityRepository,
                                         BloodInventoryBatchRepository batchRepository,
                                         ReservationService reservationService,
                                         RequestStateValidator stateValidator,
                                         NotificationService notificationService,
                                         AuditService auditService,
                                         SecurityUtils securityUtils,
                                         BloodCompatibilityPolicy compatibilityPolicy,
                                         InventoryAllocationStrategy allocationStrategy) {
        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
        this.facilityRepository = facilityRepository;
        this.batchRepository = batchRepository;
        this.reservationService = reservationService;
        this.stateValidator = stateValidator;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.compatibilityPolicy = compatibilityPolicy;
        this.allocationStrategy = allocationStrategy;
    }

    @Override
    @Transactional
    public BloodRequestDto createRequest(CreateBloodRequestDto dto) {
        User requester = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BusinessRuleException("User must be authenticated to create a request."));

        Facility hospital = requester.getFacility();
        if (hospital == null || hospital.getType() != FacilityType.HOSPITAL) {
            throw new BusinessRuleException("Only users affiliated with an approved hospital can submit blood requests.", "INVALID_REQUESTER");
        }
        if (!hospital.isApproved()) {
            throw new BusinessRuleException("Hospital facility registration is currently pending administrator approval.", "HOSPITAL_NOT_APPROVED");
        }

        String refCode = "REQ-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        BloodRequest request = new BloodRequest(
                refCode,
                hospital,
                requester,
                dto.bloodGroup(),
                dto.componentType(),
                dto.unitsRequested(),
                dto.urgency(),
                dto.requiredBy(),
                dto.patientReferenceCode(),
                dto.clinicalNotes()
        );

        // Identify target blood banks
        List<Facility> targetBanks;
        if (dto.targetBloodBankIds() != null && !dto.targetBloodBankIds().isEmpty()) {
            targetBanks = facilityRepository.findAllById(dto.targetBloodBankIds());
        } else {
            // Default: broadcast to all approved blood banks in the network
            targetBanks = facilityRepository.findByTypeAndStatus(FacilityType.BLOOD_BANK, FacilityStatus.APPROVED);
        }

        for (Facility bank : targetBanks) {
            BloodRequestResponse resp = new BloodRequestResponse(request, bank);
            request.addResponse(resp);
        }

        request.transitionStatus(RequestStatus.AWAITING_BANK_RESPONSES);
        BloodRequest saved = requestRepository.save(request);

        // Notify blood banks
        for (Facility bank : targetBanks) {
            notificationService.notifyFacility(bank,
                    "New Blood Request (" + dto.urgency().name() + ")",
                    String.format("%s submitted a %s request for %d units of %s (%s).",
                            hospital.getName(), dto.urgency().getLabel(), dto.unitsRequested(),
                            dto.bloodGroup().getDisplayName(), dto.componentType().getDisplayName()),
                    NotificationType.URGENT, "/requests/" + saved.getId());
        }

        auditService.record(requester.getId(), requester.getEmail(), requester.getRole().name(),
                hospital.getId(), "CREATE_BLOOD_REQUEST", "BloodRequest", saved.getId(),
                "Created request " + refCode + " sent to " + targetBanks.size() + " blood banks.", null);

        return BloodRequestDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BloodRequestDto getRequestById(Long requestId) {
        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));
        return BloodRequestDto.fromEntity(request);
    }

    @Override
    @Transactional(readOnly = true)
    public BloodRequestDto getRequestByReference(String reference) {
        BloodRequest request = requestRepository.findByRequestReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", reference));
        return BloodRequestDto.fromEntity(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BloodRequestDto> getHospitalRequests(Long hospitalId, Pageable pageable) {
        return requestRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId, pageable)
                .map(BloodRequestDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BloodRequestDto> getBankIncomingRequests(Long bloodBankId, Pageable pageable) {
        Page<BloodRequestResponse> responses = responseRepository.findByBloodBankIdOrderByCreatedAtDesc(bloodBankId, pageable);
        List<BloodRequestDto> dtos = responses.getContent().stream()
                .map(r -> BloodRequestDto.fromEntity(r.getRequest()))
                .distinct()
                .toList();
        return new PageImpl<>(dtos, pageable, responses.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BloodRequestDto> getAllRequests(RequestStatus status, Pageable pageable) {
        if (status != null) {
            return requestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(BloodRequestDto::fromEntity);
        }
        return requestRepository.findAll(pageable).map(BloodRequestDto::fromEntity);
    }

    @Override
    @Transactional
    public BloodRequestDto respondToRequest(Long requestId, Long bloodBankId, BankResponseActionDto actionDto) {
        User responder = securityUtils.getCurrentUser()
                .orElseThrow(() -> new BusinessRuleException("User must be authenticated."));

        if (!responder.isNetworkAdmin() && (responder.getFacility() == null || !responder.getFacility().getId().equals(bloodBankId))) {
            throw new UnauthorizedFacilityAccessException(responder.getId(), bloodBankId);
        }

        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        if (request.getStatus().isTerminal()) {
            throw new BusinessRuleException("Cannot respond to request in terminal status: " + request.getStatus());
        }

        BloodRequestResponse response = responseRepository.findByRequestIdAndBloodBankId(requestId, bloodBankId)
                .orElseGet(() -> {
                    Facility bank = facilityRepository.findById(bloodBankId)
                            .orElseThrow(() -> new ResourceNotFoundException("Facility", bloodBankId));
                    BloodRequestResponse newResp = new BloodRequestResponse(request, bank);
                    return responseRepository.save(newResp);
                });

        String action = actionDto.action().trim().toUpperCase();

        if ("ACCEPT".equals(action) || "PARTIALLY_ACCEPT".equals(action)) {
            int unitsOffered = actionDto.offeredUnits() > 0 ? actionDto.offeredUnits()
                    : (request.getUnitsRequested() - request.getUnitsFulfilled());

            if (unitsOffered <= 0) {
                throw new BusinessRuleException("Offered units must be greater than zero.");
            }

            // Find matching batches in this blood bank
            List<BloodInventoryBatch> candidateBatches = batchRepository.findActiveBatchesByFacility(
                    bloodBankId, InventoryStatus.AVAILABLE, Instant.now()
            );

            List<BloodInventoryBatch> sortedBatches = allocationStrategy.sortForAllocation(candidateBatches);
            int unitsToReserve = unitsOffered;
            int actuallyReserved = 0;

            for (BloodInventoryBatch batch : sortedBatches) {
                if (unitsToReserve <= 0) break;
                if (!compatibilityPolicy.isCompatible(request.getBloodGroup(), batch.getBloodGroup(), request.getComponentType())) {
                    continue;
                }
                int avail = batch.getAvailableQuantity();
                if (avail > 0) {
                    int take = Math.min(avail, unitsToReserve);
                    reservationService.createReservation(request, batch, take);
                    actuallyReserved += take;
                    unitsToReserve -= take;
                }
            }

            if (actuallyReserved == 0) {
                throw new BusinessRuleException("No compatible available stock found in your blood bank inventory to fulfill this reservation.", "NO_COMPATIBLE_STOCK");
            }

            if (actuallyReserved >= (request.getUnitsRequested() - request.getUnitsFulfilled())) {
                response.accept(actuallyReserved, responder, actionDto.notes());
            } else {
                response.partiallyAccept(actuallyReserved, responder, actionDto.notes());
            }

            request.recordFulfillment(actuallyReserved);

            // Notify Hospital
            notificationService.notifyFacility(request.getHospital(),
                    "Request Response Received",
                    String.format("%s accepted %d units for request %s.",
                            response.getBloodBank().getName(), actuallyReserved, request.getRequestReference()),
                    NotificationType.CONFIRMATION, "/requests/" + request.getId());

        } else if ("REJECT".equals(action)) {
            response.reject(responder, actionDto.notes());

            notificationService.notifyFacility(request.getHospital(),
                    "Request Declined by Bank",
                    String.format("%s was unable to fulfill request %s: %s",
                            response.getBloodBank().getName(), request.getRequestReference(), actionDto.notes()),
                    NotificationType.INFO, "/requests/" + request.getId());
        } else {
            throw new BusinessRuleException("Unknown response action: " + action);
        }

        responseRepository.save(response);
        BloodRequest updated = requestRepository.save(request);

        auditService.record(responder.getId(), responder.getEmail(), responder.getRole().name(),
                bloodBankId, "RESPOND_BLOOD_REQUEST", "BloodRequest", requestId,
                "Bank action: " + action + " for request " + request.getRequestReference(), null);

        return BloodRequestDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public BloodRequestDto cancelRequest(Long requestId, String reason) {
        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        stateValidator.validateTransition(request.getStatus(), RequestStatus.CANCELLED);
        request.transitionStatus(RequestStatus.CANCELLED);

        // Also release any active reservations tied to this request
        List<com.lifelink.reservation.dto.ReservationDto> reservations = reservationService.getRequestReservations(requestId);
        for (var r : reservations) {
            if (r.status() == com.lifelink.reservation.model.enums.ReservationStatus.ACTIVE) {
                reservationService.releaseReservation(r.id(), "Request cancelled by hospital: " + reason);
            }
        }

        BloodRequest saved = requestRepository.save(request);

        User currentUser = securityUtils.getCurrentUser().orElse(null);
        auditService.record(currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : "system",
                currentUser != null ? currentUser.getRole().name() : "SYSTEM",
                request.getHospital().getId(), "CANCEL_REQUEST", "BloodRequest", requestId,
                "Cancelled request " + request.getRequestReference() + ". Reason: " + reason, null);

        return BloodRequestDto.fromEntity(saved);
    }
}
