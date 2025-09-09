package com.example.hospitalqueue.web;

import com.example.hospitalqueue.service.DepartmentService;
import com.example.hospitalqueue.service.TicketService;
import com.example.hospitalqueue.web.dto.DashboardStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller cung cấp API cho Dashboard và thống kê tổng quan
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DepartmentService departmentService;
    private final TicketService ticketService;

    /**
     * Lấy thống kê tổng quan tất cả khoa
     * GET /api/dashboard/overview
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // Thống kê cơ bản
        var departments = departmentService.getAll();
        overview.put("totalDepartments", departments.size());
        overview.put("lastUpdated", LocalDateTime.now());
        
        // Thống kê từng khoa (demo)
        List<DashboardStatsDto> departmentStats = departments.stream()
            .map(dept -> {
                DashboardStatsDto stats = new DashboardStatsDto();
                stats.setDepartmentId(dept.getId());
                stats.setDepartmentCode(dept.getCode());
                stats.setDepartmentName(dept.getName());
                stats.setLocation(dept.getLocation());
                
                // Lấy thống kê thực từ ticket service
                var waiting = ticketService.waitingQueue(dept.getId());
                stats.setWaitingCount(waiting.size());
                stats.setNextNumber(waiting.isEmpty() ? null : waiting.get(0).getNumber());
                
                var latest = ticketService.latestTickets(dept.getId(), 1);
                stats.setCurrentNumber(latest.isEmpty() ? 0 : latest.get(0).getNumber());
                
                stats.setLastUpdated(LocalDateTime.now());
                stats.setStatus("ACTIVE");
                
                return stats;
            })
            .toList();
        
        overview.put("departments", departmentStats);
        
        return overview;
    }

    /**
     * Lấy thống kê chi tiết của một khoa
     * GET /api/dashboard/department/{departmentId}
     */
    @GetMapping("/department/{departmentId}")
    public DashboardStatsDto getDepartmentStats(@PathVariable Long departmentId) {
        var department = departmentService.getById(departmentId);
        
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setDepartmentId(department.getId());
        stats.setDepartmentCode(department.getCode());
        stats.setDepartmentName(department.getName());
        stats.setLocation(department.getLocation());
        
        // Thống kê chi tiết
        var waiting = ticketService.waitingQueue(departmentId);
        var latest = ticketService.latestTickets(departmentId, 10);
        
        stats.setWaitingCount(waiting.size());
        stats.setTotalToday(latest.size());
        stats.setNextNumber(waiting.isEmpty() ? null : waiting.get(0).getNumber());
        stats.setCurrentNumber(latest.isEmpty() ? 0 : latest.get(0).getNumber());
        stats.setLastUpdated(LocalDateTime.now());
        stats.setStatus("ACTIVE");
        
        return stats;
    }

    /**
     * API đơn giản cho thiết bị hiển thị
     * GET /api/dashboard/display/{departmentCode}
     */
    @GetMapping("/display/{departmentCode}")
    public Map<String, Object> getDisplayData(@PathVariable String departmentCode) {
        var department = departmentService.getAll().stream()
            .filter(d -> d.getCode().equals(departmentCode))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa: " + departmentCode));
        
        var waiting = ticketService.waitingQueue(department.getId());
        var latest = ticketService.latestTickets(department.getId(), 1);
        
        Map<String, Object> displayData = new HashMap<>();
        displayData.put("departmentName", department.getName());
        displayData.put("currentNumber", latest.isEmpty() ? 0 : latest.get(0).getNumber());
        displayData.put("waitingCount", waiting.size());
        displayData.put("nextNumber", waiting.isEmpty() ? null : waiting.get(0).getNumber());
        displayData.put("timestamp", LocalDateTime.now());
        
        return displayData;
    }
}
