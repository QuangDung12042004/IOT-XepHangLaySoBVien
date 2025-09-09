package com.example.hospitalqueue.service;

import com.example.hospitalqueue.domain.User;
import com.example.hospitalqueue.domain.UserRole;
import com.example.hospitalqueue.repository.UserRepository;
import com.example.hospitalqueue.web.dto.CreateUserRequest;
import com.example.hospitalqueue.web.dto.UpdateUserRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getActiveUsers() {
        return userRepository.findAllActiveUsers();
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRole(role);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        // Kiểm tra username và email đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getUserById(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User changePassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User updateLastLogin(String username) {
        User user = getUserByUsername(username);
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        // Soft delete bằng cách disable user
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }
}
