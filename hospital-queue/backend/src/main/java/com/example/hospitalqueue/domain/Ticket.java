package com.example.hospitalqueue.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_dept_status", columnList = "department_id,status"),
        @Index(name = "idx_ticket_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;

    @Column(nullable = false)
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketPriority priority = TicketPriority.NORMAL;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime calledAt;
    private LocalDateTime completedAt;

    // Tên người lấy số (tùy chọn)
    @Column(name = "holder_name", length = 100)
    private String holderName;

    // Liên kết với appointment nếu có
    @OneToOne(mappedBy = "ticket")
    private Appointment appointment;
}
