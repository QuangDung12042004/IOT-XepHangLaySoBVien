# Proteus Hardware Communication Setup

## Tổng quan
Backend hiện đã được setup để giao tiếp với thiết bị Proteus/Arduino thông qua 3 kênh:

### 1. HTTP REST API
- Endpoint cơ bản cho hardware: `/api/hardware/*`
- Phù hợp cho: polling, status check, command-response

### 2. WebSocket (Real-time)
- Endpoint: `ws://localhost:8080/ws/hardware`
- Phù hợp cho: real-time bidirectional communication
- Hỗ trợ: ping/pong, ticket notifications, device status

### 3. Serial Communication (COM Port)
- Mặc định: COM3 @ 9600 baud
- Có thể cấu hình qua environment variables
- Phù hợp cho: trực tiếp với Arduino/PIC

## API Endpoints cho Hardware

### HTTP Endpoints

#### Kiểm tra kết nối
```
GET /api/hardware/ping
Response: {
  "status": "online",
  "message": "Server sẵn sàng kết nối",
  "timestamp": "2025-08-30T14:50:00",
  "serial_status": "Connected to COM3 at 9600 baud"
}
```

#### Lấy và gọi số tiếp theo
```
GET /api/hardware/next-ticket/{departmentId}
Response: {
  "success": true,
  "ticket": {
    "id": 15,
    "number": 6,
    "status": "CALLED",
    "department": {"id": 1, "code": "K01", "name": "Khoa Nội"}
  },
  "message": "Đã gọi số 6 - Khoa Nội"
}
```

#### Hiển thị ticket cụ thể
```
POST /api/hardware/display-ticket
Body: {
  "ticketNumber": 6,
  "departmentCode": "K01"
}
```

#### Serial communication status
```
GET /api/hardware/serial/status
Response: {
  "connected": true,
  "connection_info": "Connected to COM3 at 9600 baud"
}
```

#### Gửi command qua serial
```
POST /api/hardware/serial/send
Body: {
  "command": "DISPLAY_TICKET:6:K01"
}
```

### WebSocket Messages

#### Kết nối
```
URL: ws://localhost:8080/ws/hardware
```

#### Message format (JSON)
```json
// Ping từ thiết bị
{"type": "ping"}

// Response
{"type": "pong", "timestamp": 1693420800000}

// Báo ticket đã được gọi
{"type": "ticket_called", "ticketId": 15, "departmentId": 1}

// Request next ticket
{"type": "request_next_ticket", "departmentId": 1}

// Device status update
{"type": "device_status", "status": "ready", "temperature": 25}
```

### Serial Protocol

#### Commands từ Server → Proteus
```
DISPLAY_TICKET:6:K01        # Hiển thị số 6 khoa K01
NEXT_TICKET:7:K02          # Thông tin ticket tiếp theo
PONG                       # Response cho ping
```

#### Commands từ Proteus → Server
```
PING                       # Kiểm tra kết nối
CALL_TICKET:6:K01         # Báo đã gọi số 6
STATUS:READY              # Báo cáo trạng thái
REQUEST_NEXT:K01          # Yêu cầu số tiếp theo
```

## Cấu hình cho Proteus

### Environment Variables
```bash
# Serial port configuration
SERIAL_PORT=COM3
SERIAL_BAUDRATE=9600
SERIAL_ENABLED=true
```

### Application.yml
```yaml
hardware:
  serial:
    port: COM3
    baudrate: 9600
    enabled: true
```

## Integration với Arduino/PIC Code

### Arduino Example (HTTP)
```cpp
#include <WiFi.h>
#include <HTTPClient.h>

void callNextTicket(int departmentId) {
    HTTPClient http;
    http.begin("http://192.168.1.100:8080/api/hardware/next-ticket/" + String(departmentId));
    
    int httpCode = http.GET();
    if (httpCode == 200) {
        String response = http.getString();
        // Parse JSON và hiển thị số ticket
        parseAndDisplay(response);
    }
    http.end();
}
```

### Arduino Example (Serial)
```cpp
void setup() {
    Serial.begin(9600);
}

void loop() {
    // Đọc command từ server
    if (Serial.available()) {
        String command = Serial.readStringUntil('\n');
        handleCommand(command);
    }
    
    // Định kỳ ping server
    static unsigned long lastPing = 0;
    if (millis() - lastPing > 30000) {
        Serial.println("PING");
        lastPing = millis();
    }
}

void handleCommand(String cmd) {
    if (cmd.startsWith("DISPLAY_TICKET:")) {
        // Parse: DISPLAY_TICKET:6:K01
        int firstColon = cmd.indexOf(':');
        int secondColon = cmd.indexOf(':', firstColon + 1);
        
        String ticketNum = cmd.substring(firstColon + 1, secondColon);
        String deptCode = cmd.substring(secondColon + 1);
        
        displayTicket(ticketNum.toInt(), deptCode);
    }
}
```

### WebSocket Example (ESP32)
```cpp
#include <WebSocketsClient.h>

WebSocketsClient webSocket;

void setup() {
    webSocket.begin("192.168.1.100", 8080, "/ws/hardware");
    webSocket.onEvent(webSocketEvent);
}

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
    switch(type) {
        case WStype_TEXT:
            String message = String((char*)payload);
            handleWebSocketMessage(message);
            break;
    }
}

void sendPing() {
    webSocket.sendTXT("{\"type\":\"ping\"}");
}
```

## Testing với Postman

1. Import collection từ `postman_collection.json`
2. Thêm các request mới cho hardware:

```
GET {{baseUrl}}/api/hardware/ping
GET {{baseUrl}}/api/hardware/serial/status
GET {{baseUrl}}/api/hardware/next-ticket/1
POST {{baseUrl}}/api/hardware/display-ticket
POST {{baseUrl}}/api/hardware/serial/send
```

## Lưu ý quan trọng

1. **Serial Port**: Đảm bảo cổng COM được chỉ định đúng và không bị ứng dụng khác sử dụng
2. **Firewall**: Mở port 8080 cho HTTP và WebSocket
3. **IP Address**: Thay `localhost` bằng IP thực của máy server khi test từ thiết bị khác
4. **Error Handling**: Backend có logging chi tiết, check console khi debug
5. **Thread Safety**: Tất cả operations đều thread-safe

## Workflow hoàn chỉnh

1. **Proteus/Arduino** ping server qua HTTP/WebSocket/Serial
2. **Server** response xác nhận kết nối
3. **Proteus** request next ticket cho department
4. **Server** call next ticket từ queue, update database
5. **Server** gửi thông tin ticket về Proteus qua serial/websocket
6. **Proteus** hiển thị số ticket trên LED/LCD
7. **Proteus** báo lại server đã hiển thị xong
8. Loop lại bước 3

Backend đã sẵn sàng cho tất cả các giao thức này!
