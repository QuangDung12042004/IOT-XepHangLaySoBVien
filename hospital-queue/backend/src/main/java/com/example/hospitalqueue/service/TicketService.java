package com.example.hospitalqueue.service;

import com.example.hospitalqueue.domain.Department;
import com.example.hospitalqueue.domain.Ticket;
import com.example.hospitalqueue.domain.TicketStatus;
import com.example.hospitalqueue.domain.TicketPriority;
import com.example.hospitalqueue.repository.DepartmentRepository;
import com.example.hospitalqueue.repository.TicketRepository;
import com.example.hospitalqueue.web.dto.QueueStatusResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;

    // Thêm các method mới để lấy tickets theo status
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatusOrderByCreatedAtAsc(status);
    }

    public List<Ticket> getTicketsByStatusAndDepartment(TicketStatus status, Long departmentId) {
        return ticketRepository.findByStatusAndDepartmentIdOrderByCreatedAtAsc(status, departmentId);
    }

    @Transactional
    public Ticket takeNumber(String departmentCode) {
        Department dept = departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khoa với code=" + departmentCode));
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        int maxToday = ticketRepository.findMaxNumberForDepartmentToday(dept.getId(), startOfDay);
        Ticket t = Ticket.builder()
                .department(dept)
                .number(maxToday + 1)
                .status(TicketStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        Ticket saved = ticketRepository.save(t);
        
        // Gửi notification
        notificationService.notifyTicketCreated(saved);
        
        return saved;
    }

    @Transactional
        public Ticket takeNumberById(Long departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khoa với ID=" + departmentId));
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        int maxToday = ticketRepository.findMaxNumberForDepartmentToday(dept.getId(), startOfDay);
        Ticket t = Ticket.builder()
                .department(dept)
                .number(maxToday + 1)
                .status(TicketStatus.WAITING)
                .priority(TicketPriority.NORMAL)
                .createdAt(LocalDateTime.now())
                .build();
        Ticket saved = ticketRepository.save(t);
        
        // Gửi notification
        notificationService.notifyTicketCreated(saved);
        
        return saved;
    }

        public Ticket takeNumberById(Long departmentId, String name) {
            // Kiểm tra tên có vé đang chờ hoặc được gọi không
            if (name != null && !name.isBlank()) {
                String trimmedName = name.trim();
                if (ticketRepository.existsByHolderNameAndActiveStatus(trimmedName)) {
                    throw new IllegalStateException("Tên '" + trimmedName + "' đã có vé đang chờ hoặc được gọi. Vui lòng hủy vé cũ trước khi lấy số mới.");
                }
            }
            
            Ticket t = takeNumberById(departmentId);
            if (name != null && !name.isBlank()) {
                    t.setHolderName(name.trim());
                    t = ticketRepository.save(t);
            }
            return t;
        }

    public List<Ticket> latestTickets(Long departmentId, int limit) {
        return ticketRepository.findByDepartmentIdOrderByCreatedAtDesc(departmentId, PageRequest.of(0, limit));
    }

    public List<Ticket> waitingQueue(Long departmentId) {
        return ticketRepository.findByDepartmentIdAndStatusOrderByCreatedAtAsc(departmentId, TicketStatus.WAITING);
    }

    public QueueStatusResponse getQueueStatus(Long departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khoa với ID=" + departmentId));
        
        // Lấy vé đang được gọi
        Ticket currentTicket = ticketRepository.findFirstByDepartmentIdAndStatusOrderByCalledAtDesc(departmentId, TicketStatus.CALLED)
                .orElse(null);
        
        // Lấy danh sách chờ
        List<Ticket> waitingTickets = waitingQueue(departmentId);
        
        // Lấy danh sách vé đã được gọi trong ngày
        List<Ticket> calledTickets = ticketRepository.findByDepartmentIdAndStatusOrderByCalledAtDesc(
                departmentId, TicketStatus.CALLED);

        // Lấy 3 vé tiếp theo
        List<Ticket> nextTicketsList = waitingTickets.stream().limit(3).collect(Collectors.toList());
        List<String> nextTickets = nextTicketsList.stream()
                .map(ticket -> dept.getCode() + String.format("%03d", ticket.getNumber()))
                .collect(Collectors.toList());
        List<String> nextDisplays = nextTicketsList.stream()
                .map(ticket -> {
                    String num = dept.getCode() + String.format("%03d", ticket.getNumber());
                    return (ticket.getHolderName() != null && !ticket.getHolderName().isBlank()) ?
                            ticket.getHolderName() + " - " + num : num;
                })
                .collect(Collectors.toList());
        
        return QueueStatusResponse.builder()
                .departmentId(departmentId)
                .departmentName(dept.getName())
                .departmentCode(dept.getCode())
                .currentTicket(currentTicket != null ? 
                        dept.getCode() + String.format("%03d", currentTicket.getNumber()) : null)
                .currentName(currentTicket != null ? currentTicket.getHolderName() : null)
                .waitingCount(waitingTickets.size())
                .estimatedWaitTime(waitingTickets.size() * 10) // 10 phút mỗi người
                .nextTickets(nextTickets)
                .nextDisplays(nextDisplays)
                .waitingTickets(waitingTickets)
                .calledTickets(calledTickets)
                .build();
    }

    @Transactional
    public Ticket callNext(Long departmentId) {
        Ticket next = ticketRepository.findFirstByDepartmentIdAndStatusOrderByCreatedAtAsc(departmentId, TicketStatus.WAITING)
                .orElseThrow(() -> new EntityNotFoundException("Không còn số chờ trong khoa này"));
        next.setStatus(TicketStatus.CALLED);
        next.setCalledAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(next);
        
        // Gửi notification
        notificationService.notifyTicketCalled(saved);
        
        // Kiểm tra và cảnh báo nếu khoa đông
        List<Ticket> waiting = waitingQueue(departmentId);
        notificationService.notifyDepartmentBusy(saved.getDepartment(), waiting.size());
        
        return saved;
    }

    @Transactional
    public Ticket complete(Long ticketId) {
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiếu với ID=" + ticketId));
        t.setStatus(TicketStatus.COMPLETED);
        t.setCompletedAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(t);
        
        // Gửi notification
        notificationService.notifyTicketCompleted(saved);
        
        return saved;
    }

    @Transactional
    public Ticket cancel(Long ticketId) {
        Ticket t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiếu với ID=" + ticketId));
        t.setStatus(TicketStatus.CANCELED);
        Ticket saved = ticketRepository.save(t);
        
        // Gửi notification
        notificationService.notifyTicketCanceled(saved);
        
        return saved;
    }

    @Transactional
    public void cancelByName(String holderName) {
        if (holderName == null || holderName.isBlank()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }
        
        String trimmedName = holderName.trim();
        List<TicketStatus> activeStatuses = Arrays.asList(TicketStatus.WAITING, TicketStatus.CALLED);
        
        Ticket ticket = ticketRepository.findFirstByHolderNameAndStatusIn(trimmedName, activeStatuses)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy vé đang chờ hoặc được gọi cho tên: " + trimmedName));
        
        ticket.setStatus(TicketStatus.CANCELED);
        Ticket saved = ticketRepository.save(ticket);
        
        // Gửi notification
        notificationService.notifyTicketCanceled(saved);
    }
    
    @Transactional
    public Ticket callSpecific(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiếu với ID=" + ticketId));
        
        if (ticket.getStatus() != TicketStatus.WAITING) {
            throw new IllegalStateException("Chỉ có thể gọi vé đang ở trạng thái WAITING");
        }
        
        ticket.setStatus(TicketStatus.CALLED);
        ticket.setCalledAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(ticket);
        
        // Gửi notification
        notificationService.notifyTicketCalled(saved);
        
        return saved;
    }
}
