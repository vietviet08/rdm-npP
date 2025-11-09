package com.rdm.service;

import com.rdm.dto.CreateDeviceDTO;
import com.rdm.dto.DeviceDTO;
import com.rdm.dto.UpdateDeviceDTO;
import com.rdm.exception.ResourceNotFoundException;
import com.rdm.model.Device;
import com.rdm.model.User;
import com.rdm.repository.DeviceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final GuacamoleService guacamoleService;

    public DeviceService(DeviceRepository deviceRepository,
            PermissionService permissionService,
            AuditService auditService,
            GuacamoleService guacamoleService) {
        this.deviceRepository = deviceRepository;
        this.permissionService = permissionService;
        this.auditService = auditService;
        this.guacamoleService = guacamoleService;
    }

    // TODO: Implement proper encryption for device passwords
    private String encryptPassword(String password) {
        // For now, store as plaintext (NOT SECURE - implement proper encryption)
        // In production, use AES encryption with a secure key
        return password;
    }

    private String decryptPassword(String encryptedPassword) {
        // For now, return as-is
        return encryptedPassword;
    }

    @Transactional(readOnly = true)
    public Page<DeviceDTO> getAllDevices(Pageable pageable, String name, Device.Protocol protocol,
            Device.DeviceStatus status) {
        Page<Device> devices;
        Integer userId = permissionService.getCurrentUserId();

        if (permissionService.isAdmin()) {
            // Admin can see all devices
            if (name != null || protocol != null || status != null) {
                devices = deviceRepository.searchDevices(name, protocol, status, pageable);
            } else {
                devices = deviceRepository.findByIsActiveTrue(pageable);
            }
        } else {
            // Non-admin users can only see devices they have access to
            // For now, return all active devices - filtering by permissions will be done in
            // Phase 3
            // TODO: Implement proper permission filtering for device list
            devices = deviceRepository.findByIsActiveTrue(pageable);
        }

        return devices.map(DeviceDTO::fromDevice);
    }

    @Transactional(readOnly = true)
    public DeviceDTO getDeviceById(Integer id) {
        Device device = deviceRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        Integer userId = permissionService.getCurrentUserId();
        if (!permissionService.isAdmin() && !permissionService.canViewDevice(userId, id)) {
            throw new ResourceNotFoundException("Device", "id", id);
        }

        return DeviceDTO.fromDevice(device);
    }

    @Transactional
    public DeviceDTO createDevice(CreateDeviceDTO createDeviceDTO, String ipAddress) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can create devices");
        }

        Device device = Device.builder()
                .name(createDeviceDTO.getName())
                .description(createDeviceDTO.getDescription())
                .host(createDeviceDTO.getHost())
                .port(createDeviceDTO.getPort())
                .protocol(createDeviceDTO.getProtocol())
                .username(createDeviceDTO.getUsername())
                .passwordEncrypted(
                        createDeviceDTO.getPassword() != null ? encryptPassword(createDeviceDTO.getPassword()) : null)
                .privateKey(createDeviceDTO.getPrivateKey())
                .tags(createDeviceDTO.getTags() != null ? createDeviceDTO.getTags() : List.of())
                .status(Device.DeviceStatus.unknown)
                .isActive(true)
                .createdBy(permissionService.getCurrentUserId())
                .build();

        Device savedDevice = deviceRepository.save(device);

        // Create Guacamole connection
        try {
            String guacamoleConnId = guacamoleService.createGuacamoleConnection(savedDevice);
            savedDevice.setGuacamoleConnId(guacamoleConnId);
            savedDevice = deviceRepository.save(savedDevice);
        } catch (Exception e) {
            // Log error but don't fail device creation
            org.slf4j.LoggerFactory.getLogger(DeviceService.class)
                    .error("Failed to create Guacamole connection for device: {}", savedDevice.getName(), e);
        }

        auditService.logAction(
                com.rdm.model.AuditLog.AuditAction.create,
                "device",
                savedDevice.getId(),
                java.util.Map.of("name", savedDevice.getName(), "host", savedDevice.getHost()),
                ipAddress);

        return DeviceDTO.fromDevice(savedDevice);
    }

    @Transactional
    public DeviceDTO updateDevice(Integer id, UpdateDeviceDTO updateDeviceDTO, String ipAddress) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can update devices");
        }

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        if (updateDeviceDTO.getName() != null)
            device.setName(updateDeviceDTO.getName());
        if (updateDeviceDTO.getDescription() != null)
            device.setDescription(updateDeviceDTO.getDescription());
        if (updateDeviceDTO.getHost() != null)
            device.setHost(updateDeviceDTO.getHost());
        if (updateDeviceDTO.getPort() != null)
            device.setPort(updateDeviceDTO.getPort());
        if (updateDeviceDTO.getProtocol() != null)
            device.setProtocol(updateDeviceDTO.getProtocol());
        if (updateDeviceDTO.getUsername() != null)
            device.setUsername(updateDeviceDTO.getUsername());
        if (updateDeviceDTO.getPassword() != null) {
            device.setPasswordEncrypted(encryptPassword(updateDeviceDTO.getPassword()));
        }
        if (updateDeviceDTO.getPrivateKey() != null)
            device.setPrivateKey(updateDeviceDTO.getPrivateKey());
        if (updateDeviceDTO.getTags() != null)
            device.setTags(updateDeviceDTO.getTags());
        if (updateDeviceDTO.getIsActive() != null)
            device.setIsActive(updateDeviceDTO.getIsActive());

        Device updatedDevice = deviceRepository.save(device);

        // Update Guacamole connection if it exists, or create if it doesn't
        try {
            if (updatedDevice.getGuacamoleConnId() != null && !updatedDevice.getGuacamoleConnId().isEmpty()) {
                guacamoleService.updateGuacamoleConnection(updatedDevice);
            } else {
                String guacamoleConnId = guacamoleService.createGuacamoleConnection(updatedDevice);
                updatedDevice.setGuacamoleConnId(guacamoleConnId);
                updatedDevice = deviceRepository.save(updatedDevice);
            }
        } catch (Exception e) {
            // Log error but don't fail device update
            org.slf4j.LoggerFactory.getLogger(DeviceService.class)
                    .error("Failed to update Guacamole connection for device: {}", updatedDevice.getName(), e);
        }

        auditService.logAction(
                com.rdm.model.AuditLog.AuditAction.update,
                "device",
                updatedDevice.getId(),
                java.util.Map.of("name", updatedDevice.getName()),
                ipAddress);

        return DeviceDTO.fromDevice(updatedDevice);
    }

    @Transactional
    public void deleteDevice(Integer id, String ipAddress) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can delete devices");
        }

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        // Delete Guacamole connection if it exists
        if (device.getGuacamoleConnId() != null && !device.getGuacamoleConnId().isEmpty()) {
            try {
                guacamoleService.deleteGuacamoleConnection(device.getGuacamoleConnId());
            } catch (Exception e) {
                // Log error but continue with device deletion
                org.slf4j.LoggerFactory.getLogger(DeviceService.class)
                        .error("Failed to delete Guacamole connection for device: {}", device.getName(), e);
            }
        }

        device.setIsActive(false);
        deviceRepository.save(device);

        auditService.logAction(
                com.rdm.model.AuditLog.AuditAction.delete,
                "device",
                id,
                java.util.Map.of("name", device.getName()),
                ipAddress);
    }
}
