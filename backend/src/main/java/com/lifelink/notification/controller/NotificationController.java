package com.lifelink.notification.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.common.dto.PageResponse;
import com.lifelink.notification.dto.NotificationDto;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.notification.service.SseNotificationService;
import com.lifelink.security.SecurityUtils;
import com.lifelink.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Endpoints for alert management and real-time SSE event streaming")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseNotificationService sseNotificationService;
    private final SecurityUtils securityUtils;

    public NotificationController(NotificationService notificationService,
                                  SseNotificationService sseNotificationService,
                                  SecurityUtils securityUtils) {
        this.notificationService = notificationService;
        this.sseNotificationService = sseNotificationService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @Operation(summary = "Get Notifications for Current User")
    public ResponseEntity<ApiResponse<PageResponse<NotificationDto>>> getMyNotifications(Pageable pageable) {
        User user = securityUtils.getCurrentUser().orElse(null);
        Long userId = user != null ? user.getId() : null;
        Long facilityId = (user != null && user.getFacility() != null) ? user.getFacility().getId() : null;
        String role = user != null ? user.getRole().name() : null;

        Page<NotificationDto> page = notificationService.getUserNotifications(userId, facilityId, role, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get Unread Notification Count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        User user = securityUtils.getCurrentUser().orElse(null);
        Long userId = user != null ? user.getId() : null;
        Long facilityId = (user != null && user.getFacility() != null) ? user.getFacility().getId() : null;
        String role = user != null ? user.getRole().name() : null;

        long unread = notificationService.getUnreadCount(userId, facilityId, role);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount", unread)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark Notification as Read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Server-Sent Events (SSE) Live Stream", description = "Subscribes the frontend to real-time notification broadcasts")
    public SseEmitter streamNotifications(@RequestParam(defaultValue = "anon") String clientId) {
        return sseNotificationService.subscribe(clientId);
    }
}
