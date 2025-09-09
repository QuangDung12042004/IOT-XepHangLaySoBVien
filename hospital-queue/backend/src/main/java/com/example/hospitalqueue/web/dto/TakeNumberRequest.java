package com.example.hospitalqueue.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TakeNumberRequest {
    @NotBlank
    private String departmentCode;
}
