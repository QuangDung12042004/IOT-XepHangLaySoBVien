package com.example.hospitalqueue.hardware;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.hospitalqueue.service.TicketService;
import com.example.hospitalqueue.domain.Ticket;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Hardware Gateway Controller cho Proteus/Arduino
 * Cung cấp HTTP endpoints để thiết bị IoT kết nối và giao tiếp
 */
@RestController
@RequestMapping("/api/hardware")
@RequiredArgsConstructor
@Slf4j
public class HardwareGatewayController {

    private final SerialCommunicationService serialService;
    private final HardwareWebSocketHandler webSocketHandler;
    private final TicketService ticketService;

    /**
     * Endpoint kiểm tra kết nối thiết bị
     * GET /api/hardware/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "online");
        response.put("message", "Server sẵn sàng kết nối");
        response.put("timestamp", LocalDateTime.now());
        response.put("serial_status", serialService.getConnectionInfo());
        log.info("Hardware ping request received");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint lấy thông tin kết nối serial
     * GET /api/hardware/serial/status
     */
    @GetMapping("/serial/status")
    public ResponseEntity<Map<String, Object>> getSerialStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("connected", serialService.isConnected());
        response.put("connection_info", serialService.getConnectionInfo());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint gửi command đến thiết bị qua serial
     * POST /api/hardware/serial/send
     */
    @PostMapping("/serial/send")
    public ResponseEntity<Map<String, Object>> sendSerialCommand(@RequestBody Map<String, String> request) {
        String command = request.get("command");
        if (command == null || command.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Command is required"));
        }

        boolean sent = serialService.sendCommand(command).join();
        
        Map<String, Object> response = new HashMap<>();
        response.put("command", command);
        response.put("sent", sent);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint nhận thông báo từ thiết bị khi đã gọi số
     * POST /api/hardware/called/{ticketId}
     */
    @PostMapping("/called/{ticketId}")
    public ResponseEntity<Map<String, Object>> deviceCalled(@PathVariable Long ticketId) {
        log.info("Device reported ticket {} has been called", ticketId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ticketId", ticketId);
        response.put("status", "acknowledged");
        response.put("timestamp", LocalDateTime.now());
        
        // TODO: Có thể thêm logic cập nhật database hoặc gửi notification
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để thiết bị lấy thông tin số tiếp theo cần gọi
     * GET /api/hardware/next-ticket/{departmentId}
     */
    @GetMapping("/next-ticket/{departmentId}")
    public ResponseEntity<Map<String, Object>> getNextTicket(@PathVariable Long departmentId) {
        log.info("Device requesting next ticket for department {}", departmentId);
        
        try {
            // Gọi next ticket và trả về thông tin
            Ticket nextTicket = ticketService.callNext(departmentId);
            
            // Gửi thông tin đến thiết bị hiển thị qua serial
            serialService.sendTicketDisplay(Long.valueOf(nextTicket.getNumber()), 
                                           nextTicket.getDepartment().getCode());
            
            // Broadcast qua WebSocket cho dashboard
            webSocketHandler.broadcastToHardware(Map.of(
                "type", "ticket_called",
                "ticketId", nextTicket.getId(),
                "ticketNumber", nextTicket.getNumber(),
                "departmentCode", nextTicket.getDepartment().getCode()
            ));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ticket", nextTicket);
            response.put("message", "Đã gọi số " + nextTicket.getNumber() + " - " + nextTicket.getDepartment().getName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error calling next ticket for department {}: {}", departmentId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("departmentId", departmentId);
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint để hiển thị ticket cụ thể trên thiết bị
     * POST /api/hardware/display-ticket
     */
    @PostMapping("/display-ticket")
    public ResponseEntity<Map<String, Object>> displayTicket(@RequestBody Map<String, Object> request) {
        try {
            Object ticketNumberObj = request.get("ticketNumber");
            String departmentCode = (String) request.get("departmentCode");
            
            if (ticketNumberObj == null || departmentCode == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ticketNumber and departmentCode are required"));
            }
            
            Long ticketNumber = Long.valueOf(ticketNumberObj.toString());
            
            // Gửi đến thiết bị hiển thị
            serialService.sendTicketDisplay(ticketNumber, departmentCode);
            
            // Broadcast qua WebSocket
            webSocketHandler.broadcastToHardware(Map.of(
                "type", "display_ticket",
                "ticketNumber", ticketNumber,
                "departmentCode", departmentCode
            ));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ticketNumber", ticketNumber);
            response.put("departmentCode", departmentCode);
            response.put("message", "Đã hiển thị số " + ticketNumber + " - " + departmentCode);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error displaying ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint để thiết bị báo cáo trạng thái
     * POST /api/hardware/status
     */
    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> deviceStatus(@RequestBody Map<String, Object> statusData) {
        log.info("Device status update: {}", statusData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("received", true);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}
