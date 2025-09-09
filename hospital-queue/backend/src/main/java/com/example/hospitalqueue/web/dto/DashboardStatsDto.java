package com.example.hospitalqueue.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO cho thống kê dashboard
 */
@Data
public class DashboardStatsDto {
    private Long departmentId;
    private String departmentCode;
    private String departmentName;
    private String location;
    
    // Thống kê hàng đợi
    private Integer waitingCount = 0;
    private Integer calledCount = 0;
    private Integer completedToday = 0;
    private Integer totalToday = 0;
    
    // Số hiện tại
    private Integer currentNumber;
    private Integer nextNumber;
    
    // Thời gian
    private LocalDateTime lastUpdated;
    private Double avgWaitingTime; // phút
    
    // Trạng thái
    private String status = "ACTIVE"; // ACTIVE, PAUSED, CLOSED
}
