package com.lifelink.notification.service;

import com.lifelink.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-Sent Events (SSE) notification streaming service.
 * Manages active subscriber emitters scoped by userId or facilityId.
 */
@Service
public class SseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SseNotificationService.class);

    // Emitter store: emitterId -> SseEmitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String clientId) {
        // 30 minute timeout
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE completed for client: {}", clientId);
            emitters.remove(clientId);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE timeout for client: {}", clientId);
            emitters.remove(clientId);
        });

        emitter.onError(e -> {
            log.debug("SSE error for client {}: {}", clientId, e.getMessage());
            emitters.remove(clientId);
        });

        try {
            // Send initial connection event
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to LifeLink Realtime Notification Stream"));
        } catch (IOException e) {
            emitters.remove(clientId);
        }

        return emitter;
    }

    public void broadcastNotification(Notification notification) {
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .data(Map.of(
                                "id", notification.getId(),
                                "title", notification.getTitle(),
                                "message", notification.getMessage(),
                                "type", notification.getType().name(),
                                "createdAt", notification.getCreatedAt().toString()
                        )));
            } catch (Exception ex) {
                log.debug("Error delivering SSE to {}: removing emitter", clientId);
                emitters.remove(clientId);
            }
        });
    }

    public void broadcastRefreshEvent(String eventType, Object data) {
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (Exception ex) {
                emitters.remove(clientId);
            }
        });
    }
}
