package com.rdm.repository;

import com.rdm.model.ConnectionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConnectionLogRepository extends JpaRepository<ConnectionLog, Integer> {
    Page<ConnectionLog> findByUserIdOrderByConnectionStartDesc(Integer userId, Pageable pageable);
    
    Page<ConnectionLog> findByDeviceIdOrderByConnectionStartDesc(Integer deviceId, Pageable pageable);
    
    @Query("SELECT cl FROM ConnectionLog cl WHERE cl.userId = :userId AND " +
           "cl.connectionStart >= :startDate AND cl.connectionStart <= :endDate " +
           "ORDER BY cl.connectionStart DESC")
    Page<ConnectionLog> findUserConnectionsByDateRange(
        @Param("userId") Integer userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}

