package com.lifelink.notification.dto;

import com.lifelink.notification.model.Notification;

import java.time.Instant;

public record NotificationDto(
    Long id,
    String title,
    String message,
    String type,
    String actionUrl,
    boolean isRead,
    Instant createdAt
) {
    public static NotificationDto fromEntity(Notification n) {
        if (n == null) return null;
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType().name(),
                n.getActionUrl(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
