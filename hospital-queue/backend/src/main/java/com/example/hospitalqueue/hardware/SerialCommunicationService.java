package com.example.hospitalqueue.hardware;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service để giao tiếp với thiết bị Proteus/Arduino qua Serial Port (COM)
 */
@Service
@Slf4j
public class SerialCommunicationService {

    @Value("${hardware.serial.port:COM3}")
    private String serialPortName;

    @Value("${hardware.serial.baudrate:9600}")
    private int baudRate;

    @Value("${hardware.serial.enabled:true}")
    private boolean serialEnabled;

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;

    @PostConstruct
    public void initialize() {
        if (!serialEnabled) {
            log.info("Serial communication is disabled");
            return;
        }

        executorService = Executors.newSingleThreadExecutor();
        
        try {
            openSerialConnection();
            if (serialPort != null && serialPort.isOpen()) {
                startListening();
                log.info("Serial communication service initialized successfully on {}", serialPortName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize serial communication: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        isRunning = false;
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        closeSerialConnection();
        log.info("Serial communication service stopped");
    }

    /**
     * Mở kết nối Serial Port
     */
    private void openSerialConnection() {
        try {
            // Liệt kê tất cả cổng COM có sẵn
            SerialPort[] availablePorts = SerialPort.getCommPorts();
            log.info("Available COM ports: ");
            for (SerialPort port : availablePorts) {
                log.info("  - {}: {}", port.getSystemPortName(), port.getPortDescription());
            }

            // Tìm và mở cổng được chỉ định
            serialPort = SerialPort.getCommPort(serialPortName);
            
            if (serialPort == null) {
                log.error("Serial port {} not found", serialPortName);
                return;
            }

            // Cấu hình serial port
            serialPort.setBaudRate(baudRate);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);

            // Mở cổng
            boolean opened = serialPort.openPort();
            if (opened) {
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();
                log.info("Serial port {} opened successfully at {} baud", serialPortName, baudRate);
            } else {
                log.error("Failed to open serial port {}", serialPortName);
            }

        } catch (Exception e) {
            log.error("Error opening serial connection: {}", e.getMessage());
        }
    }

    /**
     * Đóng kết nối Serial Port
     */
    private void closeSerialConnection() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (serialPort != null && serialPort.isOpen()) {
                serialPort.closePort();
                log.info("Serial port closed");
            }
        } catch (Exception e) {
            log.error("Error closing serial connection: {}", e.getMessage());
        }
    }

    /**
     * Bắt đầu lắng nghe dữ liệu từ serial port
     */
    private void startListening() {
        if (serialPort == null || !serialPort.isOpen()) {
            return;
        }

        isRunning = true;
        
        executorService.submit(() -> {
            byte[] buffer = new byte[1024];
            
            while (isRunning && serialPort.isOpen()) {
                try {
                    if (inputStream.available() > 0) {
                        int bytesRead = inputStream.read(buffer);
                        if (bytesRead > 0) {
                            String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8).trim();
                            handleReceivedData(receivedData);
                        }
                    }
                    Thread.sleep(50); // Giảm CPU usage
                } catch (Exception e) {
                    if (isRunning) {
                        log.error("Error reading from serial port: {}", e.getMessage());
                    }
                    break;
                }
            }
        });
    }

    /**
     * Xử lý dữ liệu nhận được từ thiết bị
     */
    private void handleReceivedData(String data) {
        log.info("Received from Proteus: {}", data);
        
        try {
            // Parse các command từ Proteus
            if (data.startsWith("PING")) {
                sendCommand("PONG");
            } else if (data.startsWith("CALL_TICKET:")) {
                String ticketInfo = data.substring("CALL_TICKET:".length());
                handleTicketCall(ticketInfo);
            } else if (data.startsWith("STATUS:")) {
                String statusInfo = data.substring("STATUS:".length());
                handleStatusUpdate(statusInfo);
            } else if (data.startsWith("REQUEST_NEXT:")) {
                String deptInfo = data.substring("REQUEST_NEXT:".length());
                handleNextTicketRequest(deptInfo);
            } else {
                log.warn("Unknown command from Proteus: {}", data);
            }
        } catch (Exception e) {
            log.error("Error handling received data: {}", e.getMessage());
        }
    }

    /**
     * Gửi command đến thiết bị qua serial
     */
    public CompletableFuture<Boolean> sendCommand(String command) {
        return CompletableFuture.supplyAsync(() -> {
            if (!serialEnabled || serialPort == null || !serialPort.isOpen()) {
                log.warn("Serial port not available, cannot send command: {}", command);
                return false;
            }

            try {
                String commandWithNewline = command + "\\n";
                byte[] commandBytes = commandWithNewline.getBytes(StandardCharsets.UTF_8);
                outputStream.write(commandBytes);
                outputStream.flush();
                log.info("Sent to Proteus: {}", command);
                return true;
            } catch (IOException e) {
                log.error("Error sending command to Proteus: {}", e.getMessage());
                return false;
            }
        });
    }

    /**
     * Gửi thông tin ticket được gọi đến thiết bị hiển thị
     */
    public void sendTicketDisplay(Long ticketNumber, String departmentCode) {
        String command = String.format("DISPLAY_TICKET:%d:%s", ticketNumber, departmentCode);
        sendCommand(command);
    }

    /**
     * Gửi thông tin next ticket đến thiết bị
     */
    public void sendNextTicketInfo(Long ticketNumber, String departmentCode) {
        String command = String.format("NEXT_TICKET:%d:%s", ticketNumber, departmentCode);
        sendCommand(command);
    }

    private void handleTicketCall(String ticketInfo) {
        log.info("Hardware reported ticket called: {}", ticketInfo);
        // TODO: Tích hợp với TicketService để cập nhật database
    }

    private void handleStatusUpdate(String statusInfo) {
        log.info("Hardware status update: {}", statusInfo);
        // TODO: Log device status to database
    }

    private void handleNextTicketRequest(String deptInfo) {
        log.info("Hardware requesting next ticket for department: {}", deptInfo);
        // TODO: Tích hợp với TicketService để lấy next ticket
    }

    /**
     * Kiểm tra trạng thái kết nối serial
     */
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen() && serialEnabled;
    }

    /**
     * Lấy thông tin cổng serial hiện tại
     */
    public String getConnectionInfo() {
        if (isConnected()) {
            return String.format("Connected to %s at %d baud", serialPortName, baudRate);
        } else {
            return "Not connected";
        }
    }
}
