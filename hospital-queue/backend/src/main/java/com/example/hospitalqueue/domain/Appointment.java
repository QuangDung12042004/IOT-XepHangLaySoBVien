package com.example.hospitalqueue.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_dept_date", columnList = "department_id,appointmentDate"),
        @Index(name = "idx_appointment_patient", columnList = "patientName,patientPhone"),
        @Index(name = "idx_appointment_status", columnList = "status,appointmentDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, length = 100)
    private String patientName;

    @Column(nullable = false, length = 20)
    private String patientPhone;

    @Column(length = 100)
    private String patientEmail;

    @Column(nullable = false)
    private LocalDateTime appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;
    private LocalDateTime canceledAt;

    // Liên kết với ticket khi check-in
    @OneToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    // Confirmation code
    @Column(length = 10, unique = true)
    private String confirmationCode;
}
