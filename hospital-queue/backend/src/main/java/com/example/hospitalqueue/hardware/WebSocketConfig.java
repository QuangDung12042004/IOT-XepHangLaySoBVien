package com.example.hospitalqueue.hardware;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import lombok.RequiredArgsConstructor;

/**
 * Cấu hình WebSocket cho giao tiếp realtime với thiết bị Proteus/Arduino
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final HardwareWebSocketHandler hardwareWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Endpoint WebSocket cho thiết bị hardware
        registry.addHandler(hardwareWebSocketHandler, "/ws/hardware")
                .setAllowedOrigins("*"); // Cho phép tất cả origin (dev mode)
        
        // Endpoint cho web dashboard monitoring
        registry.addHandler(hardwareWebSocketHandler, "/ws/dashboard")
                .setAllowedOrigins("*");
    }
}
