package com.example.hospitalqueue.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user_timestamp", columnList = "user_id,timestamp"),
        @Index(name = "idx_audit_action_timestamp", columnList = "action,timestamp"),
        @Index(name = "idx_audit_entity_timestamp", columnList = "entityType,entityId,timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // User thực hiện action

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(length = 100)
    private String entityType; // Class name của entity bị thay đổi

    @Column(length = 50)
    private String entityId; // ID của entity bị thay đổi

    @Column(nullable = false, length = 255)
    private String description; // Mô tả action

    @Column(columnDefinition = "TEXT")
    private String oldValues; // JSON của giá trị cũ

    @Column(columnDefinition = "TEXT")
    private String newValues; // JSON của giá trị mới

    @Column(length = 45)
    private String ipAddress; // IP address của user

    @Column(length = 500)
    private String userAgent; // Browser/Client info

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Metadata bổ sung
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // Success/Error status
    @Column(nullable = false)
    private Boolean success;

    @Column(length = 500)
    private String errorMessage;
}
