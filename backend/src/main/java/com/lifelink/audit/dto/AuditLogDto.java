package com.lifelink.audit.dto;

import com.lifelink.audit.model.AuditLog;

import java.time.Instant;

public record AuditLogDto(
    Long id,
    Long userId,
    String userEmail,
    String userRole,
    Long facilityId,
    String action,
    String entityType,
    Long entityId,
    String details,
    String ipAddress,
    Instant timestamp
) {
    public static AuditLogDto fromEntity(AuditLog a) {
        if (a == null) return null;
        return new AuditLogDto(
                a.getId(),
                a.getUserId(),
                a.getUserEmail(),
                a.getUserRole(),
                a.getFacilityId(),
                a.getAction(),
                a.getEntityType(),
                a.getEntityId(),
                a.getDetails(),
                a.getIpAddress(),
                a.getTimestamp()
        );
    }
}
