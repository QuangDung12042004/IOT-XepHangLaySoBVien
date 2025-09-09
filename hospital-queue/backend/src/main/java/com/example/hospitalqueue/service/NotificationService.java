package com.example.hospitalqueue.service;

import com.example.hospitalqueue.domain.*;
import com.example.hospitalqueue.repository.NotificationRepository;
import com.example.hospitalqueue.repository.UserRepository;
import com.example.hospitalqueue.hardware.HardwareWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final HardwareWebSocketHandler webSocketHandler;

    /**
     * Tạo notification mới
     */
    @Transactional
    public Notification createNotification(Long userId, NotificationType type, String title, String message, 
                                         Ticket ticket, Department department, String metadata) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .ticket(ticket)
                .department(department)
                .metadata(metadata)
                .build();

        notification = notificationRepository.save(notification);

                // Gửi thông báo qua WebSocket nếu có (tạm thời disable)
        // if (webSocketHandler != null) {
        //     try {
        //         Map<String, Object> wsMessage = Map.of(
        //             "type", "notification",
        //             "data", notification
        //         );
        //         log.info("WebSocket notification would be sent: {}", wsMessage);
        //     } catch (Exception e) {
        //         log.error("Failed to send WebSocket notification", e);
        //     }
        // }

        // Gửi email nếu cần
        if (user != null && user.getEmail() != null) {
            sendEmailNotification(user.getEmail(), title, message);
        }

        log.info("Created notification: {} for user: {}", type, userId);
        return notification;
    }

    /**
     * Notification khi tạo vé mới
     */
    public void notifyTicketCreated(Ticket ticket) {
        String title = "Vé mới được tạo";
        String message = String.format("Vé số %s đã được tạo cho khoa %s", 
                ticket.getNumber(), ticket.getDepartment().getName());
        
        // Thông báo cho admin và staff của khoa
        List<User> admins = userRepository.findActiveUsersByRole(UserRole.ADMIN);
        List<User> departmentStaff = userRepository.findByDepartmentId(ticket.getDepartment().getId());
        
        for (User admin : admins) {
            createNotification(admin.getId(), NotificationType.TICKET_CREATED, title, message, 
                    ticket, ticket.getDepartment(), null);
        }
        
        for (User staff : departmentStaff) {
            if (staff.getRole() == UserRole.STAFF) {
                createNotification(staff.getId(), NotificationType.TICKET_CREATED, title, message, 
                        ticket, ticket.getDepartment(), null);
            }
        }
    }

    /**
     * Notification khi gọi số
     */
    public void notifyTicketCalled(Ticket ticket) {
        String title = "Vé được gọi";
        String message = String.format("Vé số %s đang được gọi tại khoa %s", 
                ticket.getNumber(), ticket.getDepartment().getName());
        
        // Thông báo toàn hệ thống
        createNotification(null, NotificationType.TICKET_CALLED, title, message, 
                ticket, ticket.getDepartment(), null);
        
        // Gửi thông báo đến thiết bị hiển thị
        broadcastTicketCalled(ticket);
    }

    /**
     * Notification khi hoàn thành vé
     */
    public void notifyTicketCompleted(Ticket ticket) {
        String title = "Vé hoàn thành";
        String message = String.format("Vé số %s đã hoàn thành tại khoa %s", 
                ticket.getNumber(), ticket.getDepartment().getName());
        
        // Thông báo cho staff của khoa
        List<User> departmentStaff = userRepository.findByDepartmentId(ticket.getDepartment().getId());
        for (User staff : departmentStaff) {
            if (staff.getRole() == UserRole.STAFF) {
                createNotification(staff.getId(), NotificationType.TICKET_COMPLETED, title, message, 
                        ticket, ticket.getDepartment(), null);
            }
        }
    }

    /**
     * Notification khi hủy vé
     */
    public void notifyTicketCanceled(Ticket ticket) {
        String title = "Vé bị hủy";
        String message = String.format("Vé số %s đã bị hủy tại khoa %s", 
                ticket.getNumber(), ticket.getDepartment().getName());
        
        // Thông báo cho staff của khoa
        List<User> departmentStaff = userRepository.findByDepartmentId(ticket.getDepartment().getId());
        for (User staff : departmentStaff) {
            if (staff.getRole() == UserRole.STAFF) {
                createNotification(staff.getId(), NotificationType.TICKET_CANCELED, title, message, 
                        ticket, ticket.getDepartment(), null);
            }
        }
    }

    /**
     * Cảnh báo khi khoa đông
     */
    public void notifyDepartmentBusy(Department department, int waitingCount) {
        if (waitingCount >= 10) { // Threshold
            String title = "Khoa đông bệnh nhân";
            String message = String.format("Khoa %s hiện có %d người đang chờ", 
                    department.getName(), waitingCount);
            
            List<User> admins = userRepository.findActiveUsersByRole(UserRole.ADMIN);
            for (User admin : admins) {
                createNotification(admin.getId(), NotificationType.DEPARTMENT_BUSY, title, message, 
                        null, department, "{\"waitingCount\":" + waitingCount + "}");
            }
        }
    }

    /**
     * Lấy notifications cho user
     */
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Lấy notifications chưa đọc
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Đếm notifications chưa đọc
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Đánh dấu đã đọc
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
    }

    /**
     * Gửi qua WebSocket
     */
    private void sendWebSocketNotification(Notification notification) {
        try {
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "notification");
            wsMessage.put("notificationType", notification.getType());
            wsMessage.put("title", notification.getTitle());
            wsMessage.put("message", notification.getMessage());
            wsMessage.put("userId", notification.getUser() != null ? notification.getUser().getId() : null);
            wsMessage.put("ticketId", notification.getTicket() != null ? notification.getTicket().getId() : null);
            wsMessage.put("departmentId", notification.getDepartment() != null ? notification.getDepartment().getId() : null);
            wsMessage.put("createdAt", notification.getCreatedAt());

            // webSocketHandler.broadcastToAll(wsMessage); // Tạm thời disable
            log.info("WebSocket message would be sent: {}", wsMessage);
        } catch (Exception e) {
            log.error("Error sending WebSocket notification: {}", e.getMessage());
        }
    }

    /**
     * Gửi email notification
     */
    private void sendEmailNotification(String email, String title, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject("[Hệ thống xếp hàng] " + title);
            mailMessage.setText(message);
            mailMessage.setFrom("noreply@hospitalqueue.com");

            mailSender.send(mailMessage);
            log.info("Email notification sent to: {}", email);
        } catch (Exception e) {
            log.error("Error sending email notification to {}: {}", email, e.getMessage());
        }
    }

    /**
     * Broadcast ticket called to display devices
     */
    private void broadcastTicketCalled(Ticket ticket) {
        try {
            Map<String, Object> displayMessage = new HashMap<>();
            displayMessage.put("type", "ticket_called");
            displayMessage.put("ticketNumber", ticket.getNumber());
            displayMessage.put("departmentCode", ticket.getDepartment().getCode());
            displayMessage.put("departmentName", ticket.getDepartment().getName());
            displayMessage.put("holderName", ticket.getHolderName());
            displayMessage.put("timestamp", LocalDateTime.now());

            webSocketHandler.broadcastToHardware(displayMessage);
        } catch (Exception e) {
            log.error("Error broadcasting ticket called: {}", e.getMessage());
        }
    }

    /**
     * Cleanup old notifications (chạy scheduled)
     */
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        notificationRepository.deleteOldNotifications(oneMonthAgo);
        log.info("Cleaned up old notifications before: {}", oneMonthAgo);
    }
}
