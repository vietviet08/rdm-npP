package com.rdm.controller;

import com.rdm.dto.CreateUserDTO;
import com.rdm.dto.UpdateUserDTO;
import com.rdm.dto.UserDTO;
import com.rdm.model.User;
import com.rdm.service.UserService;
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
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody CreateUserDTO createUserDTO,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        UserDTO user = userService.createUser(createUserDTO, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserDTO updateUserDTO,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        UserDTO user = userService.updateUser(id, updateUserDTO, ipAddress);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Integer id,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        userService.deleteUser(id, ipAddress);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Integer id,
            @RequestParam User.Role role,
            HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        UserDTO user = userService.updateUserRole(id, role, ipAddress);
        return ResponseEntity.ok(user);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
