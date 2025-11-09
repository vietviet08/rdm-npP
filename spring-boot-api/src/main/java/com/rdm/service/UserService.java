package com.rdm.service;

import com.rdm.dto.CreateUserDTO;
import com.rdm.dto.UpdateUserDTO;
import com.rdm.dto.UserDTO;
import com.rdm.exception.BadRequestException;
import com.rdm.exception.ResourceNotFoundException;
import com.rdm.model.User;
import com.rdm.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PermissionService permissionService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionService = permissionService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can view all users");
        }

        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserDTO::fromUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Integer id) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can view user details");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return UserDTO.fromUser(user);
    }

    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO, String ipAddress) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can create users");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(createUserDTO.getUsername())) {
            throw new BadRequestException("Username already exists: " + createUserDTO.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new BadRequestException("Email already exists: " + createUserDTO.getEmail());
        }

        User user = User.builder()
                .username(createUserDTO.getUsername())
                .email(createUserDTO.getEmail())
                .passwordHash(passwordEncoder.encode(createUserDTO.getPassword()))
                .role(createUserDTO.getRole())
                .isActive(createUserDTO.getIsActive() != null ? createUserDTO.getIsActive() : true)
                .build();

        User savedUser = userRepository.save(user);

        auditService.logAction(
                com.rdm.model.AuditLog.AuditAction.create,
                "user",
                savedUser.getId(),
                java.util.Map.of("username", savedUser.getUsername(), "role", savedUser.getRole().name()),
                ipAddress);

        return UserDTO.fromUser(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Integer id, UpdateUserDTO updateUserDTO, String ipAddress) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can update users");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check if username is being changed and if it already exists
        if (updateUserDTO.getUsername() != null && !updateUserDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateUserDTO.getUsername())) {
                throw new BadRequestException("Username already exists: " + updateUserDTO.getUsername());
            }
            user.setUsername(updateUserDTO.getUsername());
        }

        // Check if email is being changed and if it already exists
        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateUserDTO.getEmail())) {
                throw new BadRequestException("Email already exists: " + updateUserDTO.getEmail());
            }
            user.setEmail(updateUserDTO.getEmail());
        }

        if (updateUserDTO.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        if (updateUserDTO.getRole() != null) {
            user.setRole(updateUserDTO.getRole());
        }

        if (updateUserDTO.getIsActive() != null) {
            user.setIsActive(updateUserDTO.getIsActive());
        }

        User updatedUser = userRepository.save(user);

        auditService.logAction(
                com.rdm.model.AuditLog.AuditAction.update,
                "user",
                updatedUser.getId(),
                java.util.Map.of("username", updatedUser.getUsername()),
                ipAddress);

        return UserDTO.fromUser(updatedUser);
    }

    @Transactional
    public void deleteUser(Integer id, String ipAddress) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can delete users");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Prevent deleting yourself
        Integer currentUserId = permissionService.getCurrentUserId();
        if (user.getId().equals(currentUserId)) {
            throw new BadRequestException("Cannot delete your own account");
        }

        user.setIsActive(false);
        userRepository.save(user);

        auditService.logAction(
                com.rdm.model.AuditLog.AuditAction.delete,
                "user",
                id,
                java.util.Map.of("username", user.getUsername()),
                ipAddress);
    }

    @Transactional
    public UserDTO updateUserRole(Integer id, User.Role role, String ipAddress) {
        if (!permissionService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can update user roles");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        auditService.logAction(
                com.rdm.model.AuditLog.AuditAction.update,
                "user",
                updatedUser.getId(),
                java.util.Map.of("username", updatedUser.getUsername(), "role", role.name()),
                ipAddress);

        return UserDTO.fromUser(updatedUser);
    }
}
