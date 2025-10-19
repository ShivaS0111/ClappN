package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.api.mapper.PermissionMapper;
import biz.craftline.server.feature.usermanagement.domain.model.Permission;
import biz.craftline.server.feature.usermanagement.domain.model.User;
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

/*
    *//**
     * Grant a specific permission to a user (overrides role-based permissions)
     *//*
    public boolean grantPermissionToUser(String targetUsername, String permissionName, String grantedBy, String reason) {
        try {
            User user = findUserByUsername(targetUsername);
            Permission permission = findOrCreatePermission(permissionName);

            // Check if override already exists
            Optional<User> existingOverride =
                    userPermissionOverrideRepository.findByUserAndPermission(user, permission);

            if (existingOverride.isPresent()) {
                // Update existing override
                UserPermissionOverride override = existingOverride.get();
                override.setAllowed(true);
                override.setGrantedBy(grantedBy);
                override.setReason(reason);
                userPermissionOverrideRepository.save(override);
                log.info("Updated permission override for user '{}' - granted '{}'", targetUsername, permissionName);
            } else {
                // Create new override
                UserPermissionOverride override = new UserPermissionOverride(user, permission, true, grantedBy, reason);
                userPermissionOverrideRepository.save(override);
                log.info("Created new permission override for user '{}' - granted '{}'", targetUsername, permissionName);
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to grant permission '{}' to user '{}'", permissionName, targetUsername, e);
            return false;
        }
    }

    *//**
     * Deny a specific permission for a user (overrides role-based permissions)
     *//*
    public boolean denyPermissionToUser(String targetUsername, String permissionName, String grantedBy, String reason) {
        try {
            User user = findUserByUsername(targetUsername);
            Permission permission = findOrCreatePermission(permissionName);

            // Check if override already exists
            Optional<UserPermissionOverride> existingOverride =
                    userPermissionOverrideRepository.findByUserAndPermission(user, permission);

            if (existingOverride.isPresent()) {
                // Update existing override
                UserPermissionOverride override = existingOverride.get();
                override.setAllowed(false);
                override.setGrantedBy(grantedBy);
                override.setReason(reason);
                userPermissionOverrideRepository.save(override);
                log.info("Updated permission override for user '{}' - denied '{}'", targetUsername, permissionName);
            } else {
                // Create new override
                UserPermissionOverride override = new UserPermissionOverride(user, permission, false, grantedBy, reason);
                userPermissionOverrideRepository.save(override);
                log.info("Created new permission override for user '{}' - denied '{}'", targetUsername, permissionName);
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to deny permission '{}' for user '{}'", permissionName, targetUsername, e);
            return false;
        }
    }

    *//**
     * Remove a user-specific permission override (falls back to role-based permissions)
     *//*
    public boolean removeUserPermissionOverride(String targetUsername, String permissionName) {
        try {
            User user = findUserByUsername(targetUsername);
            Permission permission = findPermissionByName(permissionName);

            if (permission == null) {
                log.warn("Permission '{}' not found", permissionName);
                return false;
            }

            Optional<UserPermissionOverride> existingOverride =
                    userPermissionOverrideRepository.findByUserAndPermission(user, permission);

            if (existingOverride.isPresent()) {
                userPermissionOverrideRepository.delete(existingOverride.get());
                log.info("Removed permission override for user '{}' - permission '{}'", targetUsername, permissionName);
                return true;
            } else {
                log.warn("No permission override found for user '{}' and permission '{}'", targetUsername, permissionName);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to remove permission override for user '{}' and permission '{}'", targetUsername, permissionName, e);
            return false;
        }
    }*/

}
