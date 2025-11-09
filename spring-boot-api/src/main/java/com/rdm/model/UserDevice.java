package com.rdm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices", schema = "app")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserDeviceId.class)
public class UserDevice {
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Id
    @Column(name = "device_id", nullable = false)
    private Integer deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType permission;

    @CreatedDate
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @Column(name = "granted_by")
    private Integer grantedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", insertable = false, updatable = false)
    private User granter;

    public enum PermissionType {
        read, write, control, view
    }
}

