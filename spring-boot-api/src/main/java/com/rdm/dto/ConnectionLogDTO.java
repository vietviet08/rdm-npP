package com.rdm.dto;

import com.rdm.model.ConnectionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionLogDTO {
    private Integer id;
    private Integer userId;
    private String username;
    private Integer deviceId;
    private String deviceName;
    private String deviceHost;
    private String protocol;
    private LocalDateTime connectionStart;
    private LocalDateTime connectionEnd;
    private Integer duration; // in seconds
    private ConnectionLog.ConnectionStatus status;
    private String ipAddress;
    private String userAgent;

    public static ConnectionLogDTO fromConnectionLog(ConnectionLog log) {
        return ConnectionLogDTO.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUser() != null ? log.getUser().getUsername() : null)
                .deviceId(log.getDeviceId())
                .deviceName(log.getDevice() != null ? log.getDevice().getName() : null)
                .deviceHost(log.getDevice() != null ? log.getDevice().getHost() : null)
                .protocol(log.getDevice() != null ? log.getDevice().getProtocol().name() : null)
                .connectionStart(log.getConnectionStart())
                .connectionEnd(log.getConnectionEnd())
                .duration(log.getDuration())
                .status(log.getStatus())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .build();
    }
}
