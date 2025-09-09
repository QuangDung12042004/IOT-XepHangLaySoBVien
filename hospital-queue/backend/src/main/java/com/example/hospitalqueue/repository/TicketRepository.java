package com.example.hospitalqueue.repository;

import com.example.hospitalqueue.domain.Ticket;
import com.example.hospitalqueue.domain.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("select coalesce(max(t.number),0) from Ticket t where t.department.id = :departmentId and t.createdAt >= :startOfDay")
    int findMaxNumberForDepartmentToday(Long departmentId, LocalDateTime startOfDay);

    List<Ticket> findByDepartmentIdAndStatusOrderByCreatedAtAsc(Long departmentId, TicketStatus status);
    List<Ticket> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId, Pageable pageable);
    List<Ticket> findByDepartmentIdAndStatusOrderByCalledAtDesc(Long departmentId, TicketStatus status);

    Optional<Ticket> findFirstByDepartmentIdAndStatusOrderByCreatedAtAsc(Long departmentId, TicketStatus status);
    Optional<Ticket> findFirstByDepartmentIdAndStatusOrderByCalledAtDesc(Long departmentId, TicketStatus status);

    // Kiểm tra tên đã có vé chưa (WAITING hoặc CALLED)
    @Query("select count(t) > 0 from Ticket t where t.holderName = :name and t.status in ('WAITING', 'CALLED')")
    boolean existsByHolderNameAndActiveStatus(String name);

    // Tìm vé đang chờ hoặc được gọi theo tên
    Optional<Ticket> findFirstByHolderNameAndStatusIn(String holderName, List<TicketStatus> statuses);
    
    // Find by status for API filtering
    List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);
    List<Ticket> findByStatusAndDepartmentIdOrderByCreatedAtAsc(TicketStatus status, Long departmentId);
    
    // Count methods for stats
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByStatus(TicketStatus status);
}
