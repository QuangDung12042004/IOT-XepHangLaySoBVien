package com.example.hospitalqueue.repository;

import com.example.hospitalqueue.domain.AuditAction;
import com.example.hospitalqueue.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Tìm theo user
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    // Tìm theo action
    Page<AuditLog> findByActionOrderByTimestampDesc(AuditAction action, Pageable pageable);
    
    // Tìm theo entity
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId, Pageable pageable);
    
    // Tìm theo thời gian
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Tìm theo user và thời gian
    List<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(Long userId, LocalDateTime start, LocalDateTime end);
    
    // Tìm audit logs thất bại
    Page<AuditLog> findBySuccessFalseOrderByTimestampDesc(Pageable pageable);
    
    // Thống kê theo action
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since GROUP BY a.action")
    List<Object[]> countByActionSince(LocalDateTime since);
    
    // Thống kê theo user
    @Query("SELECT a.user.username, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since GROUP BY a.user.username ORDER BY COUNT(a) DESC")
    List<Object[]> countByUserSince(LocalDateTime since);
    
    // Xóa audit logs cũ
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :before")
    void deleteOldAuditLogs(LocalDateTime before);
    
    // Tìm kiếm full-text
    @Query("SELECT a FROM AuditLog a WHERE " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.entityType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchAuditLogs(String searchTerm, Pageable pageable);
}
