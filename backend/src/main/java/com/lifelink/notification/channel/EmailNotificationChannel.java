package com.lifelink.notification.channel;

import com.lifelink.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Polymorphic Email delivery channel implementation.
 * Safely handles simulated delivery when no external SMTP provider is configured.
 */
@Component
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationChannel.class);

    private final boolean emailEnabled;

    public EmailNotificationChannel(@Value("${app.notifications.email.enabled:false}") boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    @Override
    public void send(Notification notification) {
        if (!isEnabled()) {
            log.debug("Email delivery disabled. Simulated dispatch for alert: {}", notification.getTitle());
            return;
        }
        // When real SMTP is configured, dispatch here.
        log.info("Sending transactional email for alert: {}", notification.getTitle());
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    @Override
    public boolean isEnabled() {
        return emailEnabled;
    }
}
