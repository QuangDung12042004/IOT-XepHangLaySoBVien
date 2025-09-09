package com.example.hospitalqueue.domain;

public enum AuditAction {
    CREATE,         // Tạo mới
    UPDATE,         // Cập nhật
    DELETE,         // Xóa
    LOGIN,          // Đăng nhập
    LOGOUT,         // Đăng xuất
    CALL_TICKET,    // Gọi số
    COMPLETE_TICKET,// Hoàn thành vé
    CANCEL_TICKET,  // Hủy vé
    SYSTEM_CONFIG,  // Thay đổi cấu hình
    EXPORT_DATA,    // Xuất dữ liệu
    IMPORT_DATA     // Nhập dữ liệu
}
