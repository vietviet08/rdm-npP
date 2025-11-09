package com.rdm.service;

import com.rdm.dto.ConnectionInitiateResponse;
import com.rdm.dto.ConnectionLogDTO;
import com.rdm.exception.BadRequestException;
import com.rdm.exception.ResourceNotFoundException;
import com.rdm.model.ConnectionLog;
import com.rdm.model.Device;
import com.rdm.model.User;
import com.rdm.model.UserDevice;
import com.rdm.repository.ConnectionLogRepository;
import com.rdm.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionService.class);

    private final ConnectionLogRepository connectionLogRepository;
    private final DeviceRepository deviceRepository;
    private final PermissionService permissionService;
    private final GuacamoleService guacamoleService;
    private final AuditService auditService;

    public ConnectionService(
            ConnectionLogRepository connectionLogRepository,
            DeviceRepository deviceRepository,
            PermissionService permissionService,
            GuacamoleService guacamoleService,
            AuditService auditService) {
        this.connectionLogRepository = connectionLogRepository;
        this.deviceRepository = deviceRepository;
        this.permissionService = permissionService;
        this.guacamoleService = guacamoleService;
        this.auditService = auditService;
    }

    /**
     * Initiate a connection to a device
     * 
     * @param deviceId  The device ID to connect to
     * @param ipAddress The client IP address
     * @param userAgent The client user agent
     * @return ConnectionInitiateResponse with connection URL and log ID
     */
    @Transactional
    public ConnectionInitiateResponse initiateConnection(Integer deviceId, String ipAddress, String userAgent) {
        logger.info("Initiating connection to device: {} from IP: {}", deviceId, ipAddress);

        try {
            Integer userId = permissionService.getCurrentUserId();
            User user = permissionService.getCurrentUser();

            // Validate device ID
            if (deviceId == null || deviceId <= 0) {
                throw new BadRequestException("Invalid device ID");
            }

            // Get device
            Device device = deviceRepository.findByIdAndIsActiveTrue(deviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

            // Validate device configuration
            validateDeviceConfiguration(device);

            // Check permission - user must have at least view permission
            if (!permissionService.isAdmin() && !permissionService.canViewDevice(userId, deviceId)) {
                logger.warn("User {} attempted to connect to device {} without permission", userId, deviceId);
                throw new BadRequestException("You do not have permission to access this device");
            }

            // Check if user has control permission (for interactive connection)
            // Viewer role can only view (read-only)
            boolean canControl = permissionService.isAdmin() ||
                    permissionService.canControlDevice(userId, deviceId);

            // Ensure Guacamole connection exists
            String guacamoleConnId = device.getGuacamoleConnId();
            if (guacamoleConnId == null || guacamoleConnId.isEmpty()) {
                logger.info("Device {} has no Guacamole connection, creating one", deviceId);
                try {
                    guacamoleConnId = guacamoleService.createGuacamoleConnection(device);
                    device.setGuacamoleConnId(guacamoleConnId);
                    deviceRepository.save(device);
                } catch (Exception e) {
                    logger.error("Failed to create Guacamole connection for device: {}", deviceId, e);
                    throw new BadRequestException("Failed to create connection: " + e.getMessage());
                }
            }

            // Create connection log
            ConnectionLog connectionLog = ConnectionLog.builder()
                    .userId(userId)
                    .deviceId(deviceId)
                    .connectionStart(LocalDateTime.now())
                    .status(ConnectionLog.ConnectionStatus.success)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            ConnectionLog savedLog = connectionLogRepository.save(connectionLog);

            // Get Guacamole connection URL
            String connectionUrl;
            try {
                connectionUrl = guacamoleService.getGuacamoleConnectionUrl(guacamoleConnId, userId);
            } catch (Exception e) {
                logger.error("Failed to get Guacamole connection URL for device: {}", deviceId, e);
                // Update connection log with failed status
                savedLog.setStatus(ConnectionLog.ConnectionStatus.failed);
                connectionLogRepository.save(savedLog);
                throw new BadRequestException("Failed to generate connection URL: " + e.getMessage());
            }

            // Log audit event
            auditService.logAction(
                    com.rdm.model.AuditLog.AuditAction.connect,
                    "connection",
                    savedLog.getId(),
                    java.util.Map.of(
                            "deviceId", deviceId,
                            "deviceName", device.getName(),
                            "protocol", device.getProtocol().name()),
                    ipAddress);

            logger.info("Connection initiated successfully. Log ID: {}, Device: {}", savedLog.getId(), deviceId);

            return ConnectionInitiateResponse.builder()
                    .connectionUrl(connectionUrl)
                    .connectionLogId(savedLog.getId())
                    .guacamoleConnId(guacamoleConnId)
                    .deviceName(device.getName())
                    .protocol(device.getProtocol().name())
                    .build();

        } catch (BadRequestException | ResourceNotFoundException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error initiating connection to device: {}", deviceId, e);
            throw new BadRequestException("Failed to initiate connection: " + e.getMessage());
        }
    }

    /**
     * Validate device configuration before connection
     * 
     * @param device The device to validate
     */
    private void validateDeviceConfiguration(Device device) {
        if (device.getHost() == null || device.getHost().trim().isEmpty()) {
            throw new BadRequestException("Device host is required");
        }

        if (device.getPort() == null || device.getPort() <= 0 || device.getPort() > 65535) {
            throw new BadRequestException("Device port must be between 1 and 65535");
        }

        if (device.getProtocol() == null) {
            throw new BadRequestException("Device protocol is required");
        }

        // Validate protocol-specific requirements
        switch (device.getProtocol()) {
            case rdp:
                if (device.getUsername() == null || device.getUsername().trim().isEmpty()) {
                    throw new BadRequestException("Username is required for RDP connections");
                }
                break;
            case vnc:
                // VNC may or may not require username depending on configuration
                break;
            case ssh:
                if ((device.getUsername() == null || device.getUsername().trim().isEmpty()) &&
                        (device.getPasswordEncrypted() == null || device.getPasswordEncrypted().trim().isEmpty()) &&
                        (device.getPrivateKey() == null || device.getPrivateKey().trim().isEmpty())) {
                    throw new BadRequestException("SSH connections require either username/password or private key");
                }
                break;
        }
    }

    /**
     * End a connection
     * 
     * @param connectionLogId The connection log ID
     * @param status          The final connection status
     */
    @Transactional
    public void endConnection(Integer connectionLogId, ConnectionLog.ConnectionStatus status) {
        logger.info("Ending connection log: {} with status: {}", connectionLogId, status);

        try {
            // Validate connection log ID
            if (connectionLogId == null || connectionLogId <= 0) {
                throw new BadRequestException("Invalid connection log ID");
            }

            ConnectionLog connectionLog = connectionLogRepository.findById(connectionLogId)
                    .orElseThrow(() -> new ResourceNotFoundException("ConnectionLog", "id", connectionLogId));

            // Check if user owns this connection or is admin
            Integer userId = permissionService.getCurrentUserId();
            if (!permissionService.isAdmin() && !connectionLog.getUserId().equals(userId)) {
                logger.warn("User {} attempted to end connection log {} owned by user {}",
                        userId, connectionLogId, connectionLog.getUserId());
                throw new BadRequestException("You do not have permission to end this connection");
            }

            // Validate status
            if (status == null) {
                status = ConnectionLog.ConnectionStatus.success;
            }

            // Update connection log
            connectionLog.setConnectionEnd(LocalDateTime.now());
            connectionLog.setStatus(status);

            // Calculate duration
            if (connectionLog.getConnectionStart() != null && connectionLog.getConnectionEnd() != null) {
                Duration duration = Duration.between(connectionLog.getConnectionStart(),
                        connectionLog.getConnectionEnd());
                connectionLog.setDuration((int) duration.getSeconds());
            }

            connectionLogRepository.save(connectionLog);

            // Log audit event
            auditService.logAction(
                    com.rdm.model.AuditLog.AuditAction.logout,
                    "connection",
                    connectionLogId,
                    java.util.Map.of(
                            "deviceId", connectionLog.getDeviceId(),
                            "status", status.name(),
                            "duration", connectionLog.getDuration() != null ? connectionLog.getDuration() : 0),
                    connectionLog.getIpAddress());

            logger.info("Connection ended. Log ID: {}, Duration: {} seconds, Status: {}",
                    connectionLogId, connectionLog.getDuration(), status);

        } catch (BadRequestException | ResourceNotFoundException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error ending connection log: {}", connectionLogId, e);
            throw new BadRequestException("Failed to end connection: " + e.getMessage());
        }
    }

    /**
     * Get connection logs for the current user
     * 
     * @param pageable Pagination parameters
     * @return Page of ConnectionLogDTO
     */
    @Transactional(readOnly = true)
    public Page<ConnectionLogDTO> getConnectionLogs(Pageable pageable) {
        try {
            Integer userId = permissionService.getCurrentUserId();

            // Validate pagination
            if (pageable.getPageSize() > 100) {
                throw new BadRequestException("Page size cannot exceed 100");
            }

            Page<ConnectionLog> logs = connectionLogRepository.findByUserIdOrderByConnectionStartDesc(userId, pageable);
            return logs.map(ConnectionLogDTO::fromConnectionLog);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting connection logs", e);
            throw new BadRequestException("Failed to get connection logs: " + e.getMessage());
        }
    }

    /**
     * Get connection logs for a specific device
     * 
     * @param deviceId The device ID
     * @param pageable Pagination parameters
     * @return Page of ConnectionLogDTO
     */
    @Transactional(readOnly = true)
    public Page<ConnectionLogDTO> getDeviceConnectionLogs(Integer deviceId, Pageable pageable) {
        try {
            // Validate device ID
            if (deviceId == null || deviceId <= 0) {
                throw new BadRequestException("Invalid device ID");
            }

            // Validate pagination
            if (pageable.getPageSize() > 100) {
                throw new BadRequestException("Page size cannot exceed 100");
            }

            // Check if device exists
            Device device = deviceRepository.findByIdAndIsActiveTrue(deviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

            // Check permission - admin can see all logs, others can only see their own
            Integer userId = permissionService.getCurrentUserId();
            if (!permissionService.isAdmin() && !permissionService.canViewDevice(userId, deviceId)) {
                logger.warn("User {} attempted to view logs for device {} without permission", userId, deviceId);
                throw new BadRequestException("You do not have permission to view logs for this device");
            }

            Page<ConnectionLog> logs = connectionLogRepository.findByDeviceIdOrderByConnectionStartDesc(deviceId,
                    pageable);
            return logs.map(ConnectionLogDTO::fromConnectionLog);
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting device connection logs for device: {}", deviceId, e);
            throw new BadRequestException("Failed to get device connection logs: " + e.getMessage());
        }
    }

    /**
     * Check if user has permission to connect to device
     * 
     * @param userId   The user ID
     * @param deviceId The device ID
     * @return true if user has permission
     */
    public boolean checkConnectionPermission(Integer userId, Integer deviceId) {
        if (permissionService.isAdmin()) {
            return true;
        }
        return permissionService.canViewDevice(userId, deviceId);
    }
}
