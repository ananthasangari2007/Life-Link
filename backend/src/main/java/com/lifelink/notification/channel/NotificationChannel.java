package com.lifelink.notification.channel;

import com.lifelink.notification.model.Notification;

/**
 * Common polymorphic interface for notification dispatch channels.
 * Demonstrates runtime Polymorphism and Strategy Pattern.
 */
public interface NotificationChannel {

    /**
     * Sends the notification through this channel.
     */
    void send(Notification notification);

    /**
     * Channel identifier (e.g., "IN_APP", "EMAIL").
     */
    String getChannelName();

    /**
     * Whether this channel is currently active.
     */
    boolean isEnabled();
}
