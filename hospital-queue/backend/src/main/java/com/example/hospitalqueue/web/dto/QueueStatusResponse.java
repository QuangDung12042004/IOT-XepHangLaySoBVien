package com.example.hospitalqueue.web.dto;

import lombok.Data;
import lombok.Builder;
import com.example.hospitalqueue.domain.Ticket;

import java.util.List;

@Data
@Builder
public class QueueStatusResponse {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String currentTicket;
    private String currentName; // tên người đang được gọi (nếu có)
    private Integer waitingCount;
    private Integer estimatedWaitTime;
    private List<String> nextTickets;
    private List<String> nextDisplays; // danh sách "Tên - Số" nếu có tên
    private List<Ticket> waitingTickets; // danh sách chi tiết vé đang chờ
    private List<Ticket> calledTickets; // danh sách vé đã được gọi trong ngày
}
