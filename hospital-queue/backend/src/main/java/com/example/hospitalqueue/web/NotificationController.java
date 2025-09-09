package com.example.hospitalqueue.web;

import com.example.hospitalqueue.domain.Notification;
import com.example.hospitalqueue.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;

    /**
     * Lấy notifications của user hiện tại
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Notification>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // Get user ID from JWT token claims
        // Tạm thời dùng username để lấy user ID
        // TODO: Lấy user ID từ JWT claims
        Long userId = 1L; // Placeholder
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Lấy notifications chưa đọc
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        Long userId = 1L; // Placeholder
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Đếm notifications chưa đọc
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = 1L; // Placeholder
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Đánh dấu notification đã đọc
     */
    @PostMapping("/{id}/mark-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long userId = 1L; // Placeholder
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Test endpoint để tạo notification (ADMIN only)
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> createTestNotification() {
        Notification notification = notificationService.createNotification(
                1L, 
                com.example.hospitalqueue.domain.NotificationType.SYSTEM_ALERT,
                "Test Notification", 
                "This is a test notification message",
                null, 
                null, 
                null
        );
        return ResponseEntity.ok(notification);
    }

    /**
     * Public endpoint để lấy thông báo công khai (không cần auth)
     */
    @GetMapping("/public")
    public ResponseEntity<List<Map<String, Object>>> getPublicNotifications() {
        // Return mock public notifications for now
        List<Map<String, Object>> publicNotifications = List.of(
            Map.of(
                "title", "Thông báo hệ thống",
                "message", "Hệ thống đang hoạt động bình thường",
                "notificationType", "SYSTEM_ALERT",
                "createdAt", java.time.LocalDateTime.now().toString()
            )
        );
        return ResponseEntity.ok(publicNotifications);
    }
}
