package com.rdm.repository;

import com.rdm.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Integer userId, Pageable pageable);
    
    Page<AuditLog> findByActionOrderByTimestampDesc(AuditLog.AuditAction action, Pageable pageable);
    
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:userId IS NULL OR al.userId = :userId) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:resourceType IS NULL OR al.resourceType = :resourceType) AND " +
           "al.timestamp >= :startDate AND al.timestamp <= :endDate " +
           "ORDER BY al.timestamp DESC")
    Page<AuditLog> searchAuditLogs(
        @Param("userId") Integer userId,
        @Param("action") AuditLog.AuditAction action,
        @Param("resourceType") String resourceType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}

