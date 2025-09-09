package com.example.hospitalqueue.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TakeNumberByIdRequest {
    @NotNull(message = "Department ID không được để trống")
    private Long departmentId;

    // Tên người lấy số (tùy chọn)
    private String name;
}
