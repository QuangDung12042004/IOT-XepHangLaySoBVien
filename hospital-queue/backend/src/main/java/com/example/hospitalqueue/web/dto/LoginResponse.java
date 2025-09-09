package com.example.hospitalqueue.web.dto;

import com.example.hospitalqueue.domain.User;
import com.example.hospitalqueue.domain.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String type; // "Bearer"
    private Long expiresIn; // seconds
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private UserRole role;
        private LocalDateTime lastLoginAt;
        private Long departmentId;
        private String departmentName;

        public static UserInfo fromUser(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .lastLoginAt(user.getLastLoginAt())
                    .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                    .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                    .build();
        }
    }
}
