package com.example.controller;

import com.example.DTO.CreateNotificationRequest;
import com.example.DTO.NotificationResponse;
import com.example.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/user/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long effectiveUserId = resolveUserId(userId);
        return ResponseEntity.ok(notificationService.getNotificationsForUser(effectiveUserId));
    }

    @GetMapping("/user/me/unread")
    public ResponseEntity<List<NotificationResponse>> getMyUnreadNotifications(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long effectiveUserId = resolveUserId(userId);
        return ResponseEntity.ok(notificationService.getUnreadNotificationsForUser(effectiveUserId));
    }

    @GetMapping("/user/me/count")
    public ResponseEntity<Long> getMyUnreadCount(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long effectiveUserId = resolveUserId(userId);
        return ResponseEntity.ok(notificationService.countUnreadNotificationsForUser(effectiveUserId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/user/me/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long effectiveUserId = resolveUserId(userId);
        notificationService.markAllAsRead(effectiveUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    private Long resolveUserId(Long userId) {
        // In production, the gateway validates JWT and sets X-User-Id header.
        // Fallback to a default if not present (for local testing).
        return userId != null ? userId : 1L;
    }
}
