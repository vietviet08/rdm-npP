package com.rdm.service;

import com.rdm.model.Device;
import com.rdm.model.User;
import com.rdm.model.UserDevice;
import com.rdm.model.UserDeviceId;
import com.rdm.repository.DeviceRepository;
import com.rdm.repository.GroupDeviceRepository;
import com.rdm.repository.GroupMemberRepository;
import com.rdm.repository.UserDeviceRepository;
import com.rdm.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final UserDeviceRepository userDeviceRepository;
    private final GroupDeviceRepository groupDeviceRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final DeviceRepository deviceRepository;

    public PermissionService(UserDeviceRepository userDeviceRepository,
            GroupDeviceRepository groupDeviceRepository,
            GroupMemberRepository groupMemberRepository,
            DeviceRepository deviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.groupDeviceRepository = groupDeviceRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.deviceRepository = deviceRepository;
    }

    public boolean hasDeviceAccess(Integer userId, Integer deviceId, UserDevice.PermissionType requiredPermission) {
        User user = getCurrentUser();

        // Admin has access to all devices
        if (user.getRole() == User.Role.admin) {
            return true;
        }

        // Check direct user permissions
        Optional<UserDevice> userDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId);
        if (userDevice.isPresent()) {
            return hasPermission(userDevice.get().getPermission(), requiredPermission);
        }

        // Check group permissions
        List<com.rdm.model.GroupDevice> groupDevices = groupDeviceRepository.findDevicesByUserGroups(userId);
        for (com.rdm.model.GroupDevice gd : groupDevices) {
            if (gd.getDeviceId().equals(deviceId) && hasPermission(gd.getPermission(), requiredPermission)) {
                return true;
            }
        }

        return false;
    }

    public boolean canViewDevice(Integer userId, Integer deviceId) {
        return hasDeviceAccess(userId, deviceId, UserDevice.PermissionType.view);
    }

    public boolean canControlDevice(Integer userId, Integer deviceId) {
        return hasDeviceAccess(userId, deviceId, UserDevice.PermissionType.control);
    }

    private boolean hasPermission(com.rdm.model.GroupDevice.PermissionType userPermission,
            UserDevice.PermissionType requiredPermission) {
        return hasPermission(convertPermission(userPermission), requiredPermission);
    }

    private boolean hasPermission(UserDevice.PermissionType userPermission,
            UserDevice.PermissionType requiredPermission) {
        if (userPermission == requiredPermission) {
            return true;
        }

        // Permission hierarchy: control > write > read > view
        switch (requiredPermission) {
            case view:
                return true; // All permissions can view
            case read:
                return userPermission == UserDevice.PermissionType.read ||
                        userPermission == UserDevice.PermissionType.write ||
                        userPermission == UserDevice.PermissionType.control;
            case write:
                return userPermission == UserDevice.PermissionType.write ||
                        userPermission == UserDevice.PermissionType.control;
            case control:
                return userPermission == UserDevice.PermissionType.control;
            default:
                return false;
        }
    }

    private UserDevice.PermissionType convertPermission(com.rdm.model.GroupDevice.PermissionType permission) {
        return UserDevice.PermissionType.valueOf(permission.name());
    }

    public boolean isAdmin() {
        User user = getCurrentUser();
        return user.getRole() == User.Role.admin;
    }

    public User getCurrentUser() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getUser();
    }

    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
