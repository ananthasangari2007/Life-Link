package com.lifelink.audit.service;

import com.lifelink.audit.dto.AuditLogDto;
import com.lifelink.audit.model.AuditLog;
import com.lifelink.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service encapsulating the audit logging operations.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(Long userId, String email, String role, Long facilityId,
                       String action, String entityType, Long entityId, String details, String ip) {
        AuditLog auditLog = new AuditLog(userId, email, role, facilityId, action, entityType, entityId, details, ip);
        auditLogRepository.save(auditLog);
        log.info("AUDIT [Action: {}, Entity: {}:{} by User: {} ({})]", action, entityType, entityId, email, role);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable).map(AuditLogDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> getFacilityLogs(Long facilityId, Pageable pageable) {
        return auditLogRepository.findByFacilityIdOrderByTimestampDesc(facilityId, pageable).map(AuditLogDto::fromEntity);
    }
}
