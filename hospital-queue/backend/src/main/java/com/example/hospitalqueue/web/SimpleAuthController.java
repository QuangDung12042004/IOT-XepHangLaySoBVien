package com.example.hospitalqueue.web;

import com.example.hospitalqueue.domain.User;
import com.example.hospitalqueue.domain.UserRole;
import com.example.hospitalqueue.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SimpleAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản không tồn tại"));
            }
            
            User user = userOpt.get();
            
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mật khẩu không đúng"));
            }
            
            if (!user.isEnabled()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tài khoản đã bị khóa"));
            }
            
            // Tạo response đơn giản
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng nhập thành công");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole().name(),
                "email", user.getEmail()
            ));
            response.put("token", "simple_token_" + user.getId()); // Token đơn giản
            
            log.info("User {} logged in successfully", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Đã có lỗi xảy ra"));
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(@RequestParam(required = false) String token) {
        // Kiểm tra token đơn giản
        if (token != null && token.startsWith("simple_token_")) {
            try {
                Long userId = Long.parseLong(token.replace("simple_token_", ""));
                Optional<User> userOpt = userRepository.findById(userId);
                
                if (userOpt.isPresent() && userOpt.get().isEnabled()) {
                    User user = userOpt.get();
                    return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "fullName", user.getFullName(),
                            "role", user.getRole().name()
                        )
                    ));
                }
            } catch (Exception e) {
                log.debug("Invalid token: {}", token);
            }
        }
        
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
