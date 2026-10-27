package com.lifelink.notification.model;

import com.lifelink.facility.model.Facility;
import com.lifelink.notification.model.enums.NotificationType;
import com.lifelink.user.model.User;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Domain entity representing an in-app and broadcast notification.
 */
@Entity
@Table(name = "lifelink_notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(name = "target_role")
    private String targetRole;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type = NotificationType.INFO;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Notification() {}

    public Notification(User recipientUser, Facility facility, String targetRole,
                        String title, String message, NotificationType type, String actionUrl) {
        this.recipientUser = recipientUser;
        this.facility = facility;
        this.targetRole = targetRole;
        this.title = title;
        this.message = message;
        this.type = type != null ? type : NotificationType.INFO;
        this.actionUrl = actionUrl;
        this.read = false;
        this.createdAt = Instant.now();
    }

    public void markAsRead() {
        this.read = true;
    }

    public Long getId() {
        return id;
    }

    public User getRecipientUser() {
        return recipientUser;
    }

    public Facility getFacility() {
        return facility;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public boolean isRead() {
        return read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
