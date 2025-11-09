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
@Table(name = "group_devices", schema = "app")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(GroupDeviceId.class)
public class GroupDevice {
    @Id
    @Column(name = "group_id", nullable = false)
    private Integer groupId;

    @Id
    @Column(name = "device_id", nullable = false)
    private Integer deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType permission;

    @CreatedDate
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private UserGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    public enum PermissionType {
        read, write, control, view
    }
}

