package com.example.auth.notification.controller;

import com.example.auth.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications : récupère toutes les notifications de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<List<NotificationService.NotificationDto>> getNotifications(@RequestAttribute("userId") Integer userId) {
        List<NotificationService.NotificationDto> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * POST /api/notifications/:id/read : marque une notification comme lue
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@RequestAttribute("userId") Integer userId, @PathVariable("id") Integer id) {
        try {
            notificationService.markAsRead(id, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/notifications/unread-count : compte les notifications non lues
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestAttribute("userId") Integer userId) {
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
