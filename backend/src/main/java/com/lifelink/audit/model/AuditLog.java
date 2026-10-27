package com.lifelink.audit.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Immutable audit trail capturing security and business-critical operations.
 */
@Entity
@Table(name = "lifelink_audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    public AuditLog() {}

    public AuditLog(Long userId, String userEmail, String userRole, Long facilityId,
                    String action, String entityType, Long entityId, String details, String ipAddress) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userRole = userRole;
        this.facilityId = facilityId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserRole() {
        return userRole;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
