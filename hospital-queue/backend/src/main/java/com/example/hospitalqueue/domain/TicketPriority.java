package com.example.hospitalqueue.domain;

public enum TicketPriority {
    LOW(1),         // Thấp - khám thường
    NORMAL(2),      // Bình thường - mặc định
    HIGH(3),        // Cao - người cao tuổi, khuyết tật
    URGENT(4),      // Khẩn cấp - cấp cứu
    VIP(5);         // VIP - khách hàng đặc biệt

    private final int level;

    TicketPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static TicketPriority fromLevel(int level) {
        for (TicketPriority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        return NORMAL;
    }
}
