package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.api.mapper.PermissionMapper;
import biz.craftline.server.feature.usermanagement.domain.model.Permission;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserAllowedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserDeniedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing user-specific permission overrides.
 * Handles the priority system:
 * 1. User Denied Permission (highest priority)
 * 2. User Allowed Permission
 * 3. Role-based permissions (lowest priority)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPermissionManagementService {

    private final UserAllowedPermissionRepository userAllowedPermissionRepository;
    private final UserDeniedPermissionRepository userDeniedPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Get all permissions explicitly allowed for a user
     */
    public List<UserAllowedPermissionEntity> getUserAllowedPermissions(Long userId) {
        return userAllowedPermissionRepository.findByUserId(userId);
    }

    /**
     * Get all permissions explicitly denied for a user
     */
    public List<UserDeniedPermissionEntity> getUserDeniedPermissions(Long userId) {
        return userDeniedPermissionRepository.findByUserId(userId);
    }


    /**
     * Get permission by name
     */
    public Permission getPermission(String permissionName) {
        return findPermissionByName(permissionName);
    }

    /**
     * Create a new permission if it doesn't exist
     */
    public Permission createPermission(String name, String description) {
        PermissionEntity entity = findOrCreatePermission(name, description);
        return PermissionMapper.toDomain(entity);
    }

    /**
     * Helper method to find permission by name
     */
    private Permission findPermissionByName(String permissionName) {
        PermissionEntity entity = permissionRepository.findByName(permissionName).orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));
        return PermissionMapper.toDomain(entity);
    }

    /**
     * Helper method to find or create permission
     */
    private PermissionEntity findOrCreatePermission(String permissionName) {
        return findOrCreatePermission(permissionName, "");
    }

    private PermissionEntity findOrCreatePermission(String permissionName, String description) {
        return permissionRepository.findByName(permissionName).orElseGet(() -> {
            return permissionRepository.save(new PermissionEntity(permissionName));
        });
    }
}
