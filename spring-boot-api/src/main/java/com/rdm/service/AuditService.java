package com.rdm.service;

import com.rdm.model.AuditLog;
import com.rdm.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Async
    public void logAction(AuditLog.AuditAction action, String resourceType, Integer resourceId, Map<String, Object> details, String ipAddress) {
        Integer userId = getCurrentUserId();
        
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details != null ? details : new HashMap<>())
                .ipAddress(ipAddress)
                .build();
        
        auditLogRepository.save(auditLog);
    }
    
    @Async
    public void logAction(AuditLog.AuditAction action, String resourceType, Integer resourceId, String ipAddress) {
        logAction(action, resourceType, resourceId, null, ipAddress);
    }
    
    @Async
    public void logAction(AuditLog.AuditAction action, String resourceType, Integer resourceId) {
        logAction(action, resourceType, resourceId, null, null);
    }
    
    private Integer getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                // Assuming UserPrincipal has getId method
                if (principal instanceof com.rdm.security.UserPrincipal) {
                    return ((com.rdm.security.UserPrincipal) principal).getId();
                }
            }
        } catch (Exception e) {
            // Ignore if no authentication context
        }
        return null;
    }
}

