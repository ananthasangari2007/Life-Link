package com.lifelink.facility.service;

import com.lifelink.audit.service.AuditService;
import com.lifelink.common.exception.ResourceNotFoundException;
import com.lifelink.facility.dto.FacilityApprovalRequest;
import com.lifelink.facility.dto.FacilityDto;
import com.lifelink.facility.model.Facility;
import com.lifelink.facility.model.enums.FacilityStatus;
import com.lifelink.facility.model.enums.FacilityType;
import com.lifelink.facility.repository.FacilityRepository;
import com.lifelink.notification.model.enums.NotificationType;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.security.SecurityUtils;
import com.lifelink.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public FacilityService(FacilityRepository facilityRepository,
                           NotificationService notificationService,
                           AuditService auditService,
                           SecurityUtils securityUtils) {
        this.facilityRepository = facilityRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public FacilityDto getFacilityById(Long id) {
        Facility f = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility", id));
        return FacilityDto.fromEntity(f);
    }

    @Transactional(readOnly = true)
    public Page<FacilityDto> getFacilitiesByStatus(FacilityStatus status, Pageable pageable) {
        return facilityRepository.findByStatus(status, pageable).map(FacilityDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<FacilityDto> getFacilitiesByType(FacilityType type, Pageable pageable) {
        return facilityRepository.findByType(type, pageable).map(FacilityDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<FacilityDto> getApprovedFacilitiesByType(FacilityType type) {
        return facilityRepository.findByTypeAndStatus(type, FacilityStatus.APPROVED)
                .stream()
                .map(FacilityDto::fromEntity)
                .toList();
    }

    @Transactional
    public FacilityDto updateApprovalStatus(Long facilityId, FacilityApprovalRequest req) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility", facilityId));

        User admin = securityUtils.getCurrentUser().orElse(null);
        Long adminId = admin != null ? admin.getId() : null;
        String adminEmail = admin != null ? admin.getEmail() : "admin@lifelink.network";

        if (Boolean.TRUE.equals(req.approve())) {
            facility.approve(req.notes());
            notificationService.notifyFacility(facility,
                    "Facility Approved",
                    "Your facility registration has been approved. You now have full access to the LifeLink network.",
                    NotificationType.CONFIRMATION, "/dashboard");
            auditService.record(adminId, adminEmail, "NETWORK_ADMIN", facilityId,
                    "APPROVE_FACILITY", "Facility", facilityId, "Approved: " + facility.getName(), null);
        } else {
            facility.reject(req.notes());
            notificationService.notifyFacility(facility,
                    "Facility Registration Rejected",
                    "Your facility registration was declined: " + req.notes(),
                    NotificationType.ALERT, "/login");
            auditService.record(adminId, adminEmail, "NETWORK_ADMIN", facilityId,
                    "REJECT_FACILITY", "Facility", facilityId, "Rejected: " + req.notes(), null);
        }

        Facility saved = facilityRepository.save(facility);
        return FacilityDto.fromEntity(saved);
    }

    @Transactional
    public FacilityDto suspendFacility(Long facilityId, String reason) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility", facilityId));

        facility.suspend(reason);
        User admin = securityUtils.getCurrentUser().orElse(null);

        auditService.record(admin != null ? admin.getId() : null,
                admin != null ? admin.getEmail() : "admin",
                "NETWORK_ADMIN", facilityId, "SUSPEND_FACILITY", "Facility", facilityId, "Suspended: " + reason, null);

        notificationService.notifyFacility(facility,
                "Facility Suspended",
                "Your facility access has been suspended: " + reason,
                NotificationType.ALERT, "/dashboard");

        return FacilityDto.fromEntity(facilityRepository.save(facility));
    }
}
