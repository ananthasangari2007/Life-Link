package com.lifelink.notification.service;

import com.lifelink.facility.model.Facility;
import com.lifelink.notification.channel.NotificationChannel;
import com.lifelink.notification.dto.NotificationDto;
import com.lifelink.notification.model.Notification;
import com.lifelink.notification.model.enums.NotificationType;
import com.lifelink.notification.repository.NotificationRepository;
import com.lifelink.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service orchestrating notifications across all registered notification channels.
 * Demonstrates runtime Polymorphism via List<NotificationChannel> injection.
 */
@Service
public class NotificationService {

    private final List<NotificationChannel> channels;
    private final NotificationRepository notificationRepository;

    public NotificationService(List<NotificationChannel> channels, NotificationRepository notificationRepository) {
        this.channels = channels;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void notifyUser(User user, String title, String message, NotificationType type, String actionUrl) {
        Notification notification = new Notification(user, user.getFacility(), null, title, message, type, actionUrl);
        dispatchToChannels(notification);
    }

    @Transactional
    public void notifyFacility(Facility facility, String title, String message, NotificationType type, String actionUrl) {
        Notification notification = new Notification(null, facility, null, title, message, type, actionUrl);
        dispatchToChannels(notification);
    }

    @Transactional
    public void notifyRole(String role, String title, String message, NotificationType type, String actionUrl) {
        Notification notification = new Notification(null, null, role, title, message, type, actionUrl);
        dispatchToChannels(notification);
    }

    private void dispatchToChannels(Notification notification) {
        // Dispatches through all enabled polymorphic channels (InApp, Email, etc.)
        for (NotificationChannel channel : channels) {
            if (channel.isEnabled()) {
                channel.send(notification);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Long facilityId, String role, Pageable pageable) {
        return notificationRepository.findUserNotifications(userId, facilityId, role, pageable)
                .map(NotificationDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId, Long facilityId, String role) {
        return notificationRepository.countUnreadNotifications(userId, facilityId, role);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(Notification::markAsRead);
    }
}
