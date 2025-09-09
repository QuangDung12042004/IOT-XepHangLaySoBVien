package com.example.hospitalqueue.hardware;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * WebSocket Handler cho giao tiếp với thiết bị Proteus/Arduino
 */
@Component
@Slf4j
public class HardwareWebSocketHandler implements WebSocketHandler {

    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        activeSessions.put(sessionId, session);
        log.info("Hardware WebSocket connection established: {}", sessionId);
        
        // Gửi message chào mừng
        sendMessage(session, Map.of(
            "type", "connection_established",
            "message", "Kết nối thành công với server",
            "sessionId", sessionId
        ));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        log.info("Received WebSocket message from {}: {}", session.getId(), payload);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            handleHardwareMessage(session, messageData);
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage());
            sendErrorMessage(session, "Invalid message format");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        activeSessions.remove(sessionId);
        log.info("Hardware WebSocket connection closed: {} ({})", sessionId, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Xử lý message từ thiết bị hardware
     */
    private void handleHardwareMessage(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        String type = (String) messageData.get("type");
        
        switch (type) {
            case "ping":
                sendMessage(session, Map.of("type", "pong", "timestamp", System.currentTimeMillis()));
                break;
                
            case "ticket_called":
                handleTicketCalled(session, messageData);
                break;
                
            case "device_status":
                handleDeviceStatus(session, messageData);
                break;
                
            case "request_next_ticket":
                handleRequestNextTicket(session, messageData);
                break;
                
            default:
                sendErrorMessage(session, "Unknown message type: " + type);
        }
    }

    private void handleTicketCalled(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        Object ticketId = messageData.get("ticketId");
        Object departmentId = messageData.get("departmentId");
        
        log.info("Hardware reported ticket called - ticketId: {}, departmentId: {}", ticketId, departmentId);
        
        // TODO: Cập nhật database ticket status
        // TODO: Broadcast to dashboard clients
        
        sendMessage(session, Map.of(
            "type", "ticket_called_ack",
            "ticketId", ticketId,
            "status", "acknowledged"
        ));
    }

    private void handleDeviceStatus(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        log.info("Device status update: {}", messageData);
        
        sendMessage(session, Map.of(
            "type", "status_ack",
            "received", true
        ));
    }

    private void handleRequestNextTicket(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        Object departmentId = messageData.get("departmentId");
        
        // TODO: Tích hợp với TicketService để lấy ticket tiếp theo
        
        sendMessage(session, Map.of(
            "type", "next_ticket_info",
            "departmentId", departmentId,
            "message", "Use REST API /api/tickets/call-next/" + departmentId
        ));
    }

    /**
     * Gửi message đến WebSocket client
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    private void sendErrorMessage(WebSocketSession session, String error) throws IOException {
        sendMessage(session, Map.of("type", "error", "message", error));
    }

    /**
     * Broadcast message tới tất cả thiết bị đang kết nối
     */
    public void broadcastToHardware(Map<String, Object> message) {
        activeSessions.values().forEach(session -> {
            try {
                sendMessage(session, message);
            } catch (IOException e) {
                log.error("Failed to broadcast message to session {}: {}", session.getId(), e.getMessage());
            }
        });
    }

    /**
     * Gửi message đến thiết bị cụ thể (nếu biết sessionId)
     */
    public void sendToHardware(String sessionId, Map<String, Object> message) {
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
            } catch (IOException e) {
                log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
            }
        }
    }
}
