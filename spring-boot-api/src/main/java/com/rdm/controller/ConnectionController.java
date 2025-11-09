package com.rdm.controller;

import com.rdm.dto.ConnectionInitiateResponse;
import com.rdm.dto.ConnectionLogDTO;
import com.rdm.model.ConnectionLog;
import com.rdm.service.ConnectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * Initiate a connection to a device
     * POST /api/connections/{deviceId}/initiate
     */
    @PostMapping("/{deviceId}/initiate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ConnectionInitiateResponse> initiateConnection(
            @PathVariable Integer deviceId,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        ConnectionInitiateResponse response = connectionService.initiateConnection(
                deviceId, ipAddress, userAgent);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * End a connection
     * POST /api/connections/{connectionLogId}/end
     */
    @PostMapping("/{connectionLogId}/end")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<Void> endConnection(
            @PathVariable Integer connectionLogId,
            @RequestBody(required = false) EndConnectionRequest request) {
        ConnectionLog.ConnectionStatus status = request != null && request.getStatus() != null
                ? request.getStatus()
                : ConnectionLog.ConnectionStatus.success;

        connectionService.endConnection(connectionLogId, status);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get connection logs for the current user
     * GET /api/connections
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<Page<ConnectionLogDTO>> getConnectionLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ConnectionLogDTO> logs = connectionService.getConnectionLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get connection logs for a specific device
     * GET /api/connections/device/{deviceId}/logs
     */
    @GetMapping("/device/{deviceId}/logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<Page<ConnectionLogDTO>> getDeviceConnectionLogs(
            @PathVariable Integer deviceId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ConnectionLogDTO> logs = connectionService.getDeviceConnectionLogs(deviceId, pageable);
        return ResponseEntity.ok(logs);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Request body for ending a connection
     */
    public static class EndConnectionRequest {
        @NotNull
        private ConnectionLog.ConnectionStatus status;

        public ConnectionLog.ConnectionStatus getStatus() {
            return status;
        }

        public void setStatus(ConnectionLog.ConnectionStatus status) {
            this.status = status;
        }
    }
}
