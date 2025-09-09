package com.example.hospitalqueue;

import com.example.hospitalqueue.domain.Department;
import com.example.hospitalqueue.domain.Ticket;
import com.example.hospitalqueue.domain.TicketStatus;
import com.example.hospitalqueue.repository.DepartmentRepository;
import com.example.hospitalqueue.repository.TicketRepository;
import com.example.hospitalqueue.service.TicketService;
import com.example.hospitalqueue.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TicketServiceTest {
    @Test
    void takeNumber_shouldIncrease() {
        TicketRepository tr = Mockito.mock(TicketRepository.class);
        DepartmentRepository dr = Mockito.mock(DepartmentRepository.class);
        NotificationService ns = Mockito.mock(NotificationService.class);
        TicketService svc = new TicketService(tr, dr, ns);

        Department d = Department.builder().id(1L).code("K01").name("Khoa Nội").build();
        Mockito.when(dr.findByCode("K01")).thenReturn(Optional.of(d));
        Mockito.when(tr.findMaxNumberForDepartmentToday(Mockito.eq(1L), Mockito.any(LocalDateTime.class))).thenReturn(5);
        Mockito.when(tr.save(Mockito.any(Ticket.class))).thenAnswer(i -> i.getArguments()[0]);

        Ticket t = svc.takeNumber("K01");
        assertThat(t.getNumber()).isEqualTo(6);
        assertThat(t.getStatus()).isEqualTo(TicketStatus.WAITING);
    }
}