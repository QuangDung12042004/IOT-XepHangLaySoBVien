package com.example.hospitalqueue.domain;

public enum NotificationType {
    TICKET_CREATED,     // Vé được tạo
    TICKET_CALLED,      // Vé được gọi
    TICKET_COMPLETED,   // Vé hoàn thành
    TICKET_CANCELED,    // Vé bị hủy
    SYSTEM_ALERT,       // Cảnh báo hệ thống
    DEPARTMENT_BUSY     // Khoa đông bệnh nhân
}
