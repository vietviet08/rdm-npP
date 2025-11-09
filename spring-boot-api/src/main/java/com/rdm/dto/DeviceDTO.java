package com.rdm.dto;

import com.rdm.model.Device;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDTO {
    private Integer id;
    private String name;
    private String description;
    private String host;
    private Integer port;
    private Device.Protocol protocol;
    private String username;
    private Device.DeviceStatus status;
    private List<String> tags;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    
    public static DeviceDTO fromDevice(Device device) {
        return DeviceDTO.builder()
                .id(device.getId())
                .name(device.getName())
                .description(device.getDescription())
                .host(device.getHost())
                .port(device.getPort())
                .protocol(device.getProtocol())
                .username(device.getUsername())
                .status(device.getStatus())
                .tags(device.getTags())
                .isActive(device.getIsActive())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .createdBy(device.getCreatedBy())
                .build();
    }
}

