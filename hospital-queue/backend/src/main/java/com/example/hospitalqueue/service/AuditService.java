package com.example.hospitalqueue.service;

import com.example.hospitalqueue.domain.AuditAction;
import com.example.hospitalqueue.domain.AuditLog;
import com.example.hospitalqueue.domain.User;
import com.example.hospitalqueue.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    /**
     * Log audit event
     */
    @Transactional
    public AuditLog logAudit(AuditAction action, String entityType, String entityId, 
                            String description, Object oldValues, Object newValues) {
        try {
            User currentUser = getCurrentUser();
            HttpServletRequest request = getCurrentRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .user(currentUser)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId != null ? entityId.toString() : null)
                    .description(description)
                    .oldValues(toJson(oldValues))
                    .newValues(toJson(newValues))
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();
            
            return auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            log.error("Error logging audit event: {}", e.getMessage());
            return logErrorAudit(action, entityType, entityId, description, e.getMessage());
        }
    }

    /**
     * Log audit event với metadata
     */
    @Transactional
    public AuditLog logAuditWithMetadata(AuditAction action, String entityType, String entityId, 
                                       String description, Object oldValues, Object newValues, 
                                       Map<String, Object> metadata) {
        try {
            User currentUser = getCurrentUser();
            HttpServletRequest request = getCurrentRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .user(currentUser)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId != null ? entityId.toString() : null)
                    .description(description)
                    .oldValues(toJson(oldValues))
                    .newValues(toJson(newValues))
                    .metadata(toJson(metadata))
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();
            
            return auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            log.error("Error logging audit event with metadata: {}", e.getMessage());
            return logErrorAudit(action, entityType, entityId, description, e.getMessage());
        }
    }

    /**
     * Log failed audit event
     */
    @Transactional
    public AuditLog logErrorAudit(AuditAction action, String entityType, String entityId, 
                                 String description, String errorMessage) {
        try {
            User currentUser = getCurrentUser();
            HttpServletRequest request = getCurrentRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .user(currentUser)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId != null ? entityId.toString() : null)
                    .description(description)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .timestamp(LocalDateTime.now())
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();
            
            return auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            log.error("Critical error: Cannot log audit event: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lấy audit logs
     */
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Lấy audit logs theo user
     */
    public Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * Lấy audit logs theo action
     */
    public Page<AuditLog> getAuditLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }

    /**
     * Lấy audit logs theo entity
     */
    public Page<AuditLog> getAuditLogsByEntity(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);
    }

    /**
     * Lấy audit logs theo thời gian
     */
    public Page<AuditLog> getAuditLogsByTimeRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageable);
    }

    /**
     * Tìm kiếm audit logs
     */
    public Page<AuditLog> searchAuditLogs(String searchTerm, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(searchTerm, pageable);
    }

    /**
     * Lấy failed audit logs
     */
    public Page<AuditLog> getFailedAuditLogs(Pageable pageable) {
        return auditLogRepository.findBySuccessFalseOrderByTimestampDesc(pageable);
    }

    /**
     * Thống kê audit by action
     */
    public List<Object[]> getAuditStatsByAction(LocalDateTime since) {
        return auditLogRepository.countByActionSince(since);
    }

    /**
     * Thống kê audit by user
     */
    public List<Object[]> getAuditStatsByUser(LocalDateTime since) {
        return auditLogRepository.countByUserSince(since);
    }

    /**
     * Cleanup old audit logs
     */
    @Transactional
    public void cleanupOldAuditLogs() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        auditLogRepository.deleteOldAuditLogs(threeMonthsAgo);
        log.info("Cleaned up old audit logs before: {}", threeMonthsAgo);
    }

    // Helper methods
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                return userService.getUserByUsername(authentication.getName());
            }
        } catch (Exception e) {
            log.debug("Cannot get current user: {}", e.getMessage());
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    private String toJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Cannot serialize object to JSON: {}", e.getMessage());
            return object.toString();
        }
    }
}
