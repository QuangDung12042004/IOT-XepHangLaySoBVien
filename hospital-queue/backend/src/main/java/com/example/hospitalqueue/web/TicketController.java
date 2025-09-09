package com.example.hospitalqueue.web;

import com.example.hospitalqueue.domain.Ticket;
import com.example.hospitalqueue.domain.TicketStatus;
import com.example.hospitalqueue.service.TicketService;
import com.example.hospitalqueue.web.dto.TakeNumberRequest;
import com.example.hospitalqueue.web.dto.TakeNumberByIdRequest;
import com.example.hospitalqueue.web.dto.QueueStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @PostMapping("/take-number")
    @ResponseStatus(HttpStatus.CREATED)
    public Ticket takeNumber(@Valid @RequestBody TakeNumberByIdRequest request) {
        return ticketService.takeNumberById(request.getDepartmentId(), request.getName());
    }

    // API để lấy danh sách tickets theo status
    @GetMapping
    public List<Ticket> getTickets(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) Long departmentId) {
        if (status != null) {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            if (departmentId != null) {
                return ticketService.getTicketsByStatusAndDepartment(ticketStatus, departmentId);
            } else {
                return ticketService.getTicketsByStatus(ticketStatus);
            }
        }
        return ticketService.getAllTickets();
    }

    // Giữ lại API cũ để tương thích
    @PostMapping("/take-number-by-code")
    @ResponseStatus(HttpStatus.CREATED)
    public Ticket takeNumberByCode(@Valid @RequestBody TakeNumberRequest request) {
        return ticketService.takeNumber(request.getDepartmentCode());
    }

    @GetMapping("/waiting/{departmentId}")
    public List<Ticket> waiting(@PathVariable Long departmentId) {
        return ticketService.waitingQueue(departmentId);
    }

    @GetMapping("/queue-status/{departmentId}")
    public QueueStatusResponse queueStatus(@PathVariable Long departmentId) {
        return ticketService.getQueueStatus(departmentId);
    }

    @GetMapping("/latest/{departmentId}")
    public List<Ticket> latest(@PathVariable Long departmentId,
                               @RequestParam(defaultValue = "10") int limit) {
        return ticketService.latestTickets(departmentId, Math.max(1, Math.min(limit, 100)));
    }

    @PostMapping("/call-next/{departmentId}")
    public Ticket callNext(@PathVariable Long departmentId) {
        return ticketService.callNext(departmentId);
    }

    @PostMapping("/{ticketId}/call")
    public Ticket callSpecific(@PathVariable Long ticketId) {
        return ticketService.callSpecific(ticketId);
    }

    @PostMapping("/complete/{ticketId}")
    public Ticket complete(@PathVariable Long ticketId) {
        return ticketService.complete(ticketId);
    }

    @PostMapping("/cancel/{ticketId}")
    public Ticket cancel(@PathVariable Long ticketId) {
        return ticketService.cancel(ticketId);
    }

    @PostMapping("/cancel-by-name")
    public void cancelByName(@RequestParam String name) {
        ticketService.cancelByName(name);
    }
}
