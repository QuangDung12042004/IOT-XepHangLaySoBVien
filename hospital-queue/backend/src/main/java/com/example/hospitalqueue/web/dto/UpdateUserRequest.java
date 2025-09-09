package com.example.hospitalqueue.web.dto;

import com.example.hospitalqueue.domain.UserRole;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email(message = "Email should be valid")
    private String email;

    private String fullName;
    private String phone;
    private UserRole role;
    private Boolean enabled;
}
