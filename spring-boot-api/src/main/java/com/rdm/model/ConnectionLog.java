package com.rdm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connection_logs", schema = "app")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "device_id", nullable = false)
    private Integer deviceId;

    @Column(name = "connection_start", nullable = false)
    private LocalDateTime connectionStart;

    @Column(name = "connection_end")
    private LocalDateTime connectionEnd;

    @Column
    private Integer duration; // in seconds

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionStatus status;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    public enum ConnectionStatus {
        success, failed, timeout
    }
}

