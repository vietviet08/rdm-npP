package com.rdm.controller;

import com.rdm.dto.CreateDeviceDTO;
import com.rdm.dto.DeviceDTO;
import com.rdm.dto.UpdateDeviceDTO;
import com.rdm.model.Device;
import com.rdm.service.DeviceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeviceController {
    
    private final DeviceService deviceService;
    
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
    
    @GetMapping
    public ResponseEntity<Page<DeviceDTO>> getAllDevices(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Device.Protocol protocol,
            @RequestParam(required = false) Device.DeviceStatus status) {
        Page<DeviceDTO> devices = deviceService.getAllDevices(pageable, name, protocol, status);
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Integer id) {
        DeviceDTO device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDTO> createDevice(
            @Valid @RequestBody CreateDeviceDTO createDeviceDTO,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        DeviceDTO device = deviceService.createDevice(createDeviceDTO, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDTO> updateDevice(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateDeviceDTO updateDeviceDTO,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        DeviceDTO device = deviceService.updateDevice(id, updateDeviceDTO, ipAddress);
        return ResponseEntity.ok(device);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable Integer id,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        deviceService.deleteDevice(id, ipAddress);
        return ResponseEntity.noContent().build();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

