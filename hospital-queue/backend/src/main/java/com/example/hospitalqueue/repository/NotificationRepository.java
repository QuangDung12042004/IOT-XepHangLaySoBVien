package com.example.hospitalqueue.repository;

import com.example.hospitalqueue.domain.Notification;
import com.example.hospitalqueue.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy notifications cho user cụ thể
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Lấy notifications chưa đọc cho user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    // Đếm notifications chưa đọc
    long countByUserIdAndIsReadFalse(Long userId);
    
    // Lấy notifications toàn hệ thống (user_id = null)
    Page<Notification> findByUserIsNullOrderByCreatedAtDesc(Pageable pageable);
    
    // Lấy notifications theo type
    List<Notification> findByTypeAndCreatedAtAfterOrderByCreatedAtDesc(NotificationType type, LocalDateTime after);
    
    // Đánh dấu đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :id")
    void markAsRead(Long id, LocalDateTime readAt);
    
    // Đánh dấu tất cả đã đọc cho user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadForUser(Long userId, LocalDateTime readAt);
    
    // Xóa notifications cũ
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :before")
    void deleteOldNotifications(LocalDateTime before);
}
