package com.lifelink.notification.channel;

import com.lifelink.notification.model.Notification;
import com.lifelink.notification.repository.NotificationRepository;
import com.lifelink.notification.service.SseNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * In-App delivery channel persisting alerts to database and pushing via SSE.
 */
@Component
public class InAppNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationChannel.class);

    private final NotificationRepository notificationRepository;
    private final SseNotificationService sseNotificationService;

    public InAppNotificationChannel(NotificationRepository notificationRepository,
                                    @Lazy SseNotificationService sseNotificationService) {
        this.notificationRepository = notificationRepository;
        this.sseNotificationService = sseNotificationService;
    }

    @Override
    public void send(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        log.info("Dispatched IN-APP notification [ID: {}]: {}", saved.getId(), saved.getTitle());
        sseNotificationService.broadcastNotification(saved);
    }

    @Override
    public String getChannelName() {
        return "IN_APP";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
