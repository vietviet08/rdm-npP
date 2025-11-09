package com.rdm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "devices", schema = "app")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Protocol protocol;

    @Column(length = 255)
    private String username;

    @Column(name = "password_encrypted", columnDefinition = "TEXT")
    private String passwordEncrypted;

    @Column(name = "private_key", columnDefinition = "TEXT")
    private String privateKey;

    @Column(name = "guacamole_conn_id", length = 255)
    private String guacamoleConnId;

    @Enumerated(EnumType.STRING)
    @Column
    @Builder.Default
    private DeviceStatus status = DeviceStatus.unknown;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> tags = List.of();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;

    public enum Protocol {
        rdp, vnc, ssh
    }

    public enum DeviceStatus {
        online, offline, unknown
    }
}
