package com.example.hospitalqueue.repository;

import com.example.hospitalqueue.domain.User;
import com.example.hospitalqueue.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
    List<User> findByDepartmentId(Long departmentId);
    
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveUsersByRole(UserRole role);
}
