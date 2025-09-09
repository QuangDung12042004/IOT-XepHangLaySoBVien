package com.example.hospitalqueue.domain;

public enum AppointmentStatus {
    PENDING,        // Chờ xác nhận
    CONFIRMED,      // Đã xác nhận
    CHECKED_IN,     // Đã check-in (đã lấy số)
    COMPLETED,      // Đã hoàn thành
    CANCELED,       // Đã hủy
    NO_SHOW         // Không đến khám
}
