package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.infra.entity.*;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserAllowedPermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserDeniedPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RBACService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAllowedPermissionRepository userAllowedPermissionRepository;

    @Autowired
    private UserDeniedPermissionRepository userDeniedPermissionRepository;

    public boolean hasPermission(RoleEntity role, String permissionName) {
        Set<PermissionEntity> permissions = role.getPermissions();
        if (permissions == null) return false;
        return permissions.stream().anyMatch(p -> p.getName().equalsIgnoreCase(permissionName));
    }

    /**
     * Get user permissions with hierarchical priority:
     * 1. User Denied Permissions (highest priority - always deny)
     * 2. User Allowed Permissions (override role permissions)
     * 3. Role-based Permissions (default/fallback)
     */
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(String username) {
        try {
            // Use the JOIN FETCH query to load everything in one go
            Optional<UserEntity> userOpt = userRepository.findByEmailWithRolesAndPermissions(username);

            if (userOpt.isEmpty()) {
                return List.of("ROLE_USER"); // Return default permission if user not found
            }

            UserEntity user = userOpt.get();

            // Get all role-based permissions first - safely
            Set<String> rolePermissions = new HashSet<>();
            try {
                Set<RoleEntity> roles = user.getRoles();
                if (roles != null && !roles.isEmpty()) {
                    for (RoleEntity role : roles) {
                        Set<PermissionEntity> permissions = role.getPermissions();
                        if (permissions != null) {
                            for (PermissionEntity permission : permissions) {
                                rolePermissions.add(permission.getName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Get user-specific allowed permissions - safely
            List<String> userAllowed = new ArrayList<>();
            try {
                List<UserAllowedPermissionEntity> allowedEntities = userAllowedPermissionRepository.findByUserId(user.getId());
                for (UserAllowedPermissionEntity entity : allowedEntities) {
                    if (entity.getPermission() != null) {
                        userAllowed.add(entity.getPermission().getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Get user-specific denied permissions - safely
            List<String> userDenied = new ArrayList<>();
            try {
                List<UserDeniedPermissionEntity> deniedEntities = userDeniedPermissionRepository.findByUserId(user.getId());
                for (UserDeniedPermissionEntity entity : deniedEntities) {
                    if (entity.getPermission() != null) {
                        userDenied.add(entity.getPermission().getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Apply hierarchical permission logic
            Set<String> finalPermissions = new HashSet<>(rolePermissions);

            // Add user-specific allowed permissions (priority 2)
            finalPermissions.addAll(userAllowed);

            // Remove user-specific denied permissions (priority 1 - highest)
            userDenied.forEach(finalPermissions::remove);

            // Ensure user has at least basic role
            if (finalPermissions.isEmpty()) {
                finalPermissions.add("ROLE_USER");
            }

            return new ArrayList<>(finalPermissions);

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("ROLE_USER"); // Return safe default on any error
        }
    }

    /**
     * Check if user has permission with hierarchical priority system
     */
    public boolean userHasPermission(String username, String permissionName) {
        return userRepository.findByEmail(username)
            .map(user -> checkUserPermissionHierarchy(user, permissionName))
            .orElse(false);
    }

    /**
     * Hierarchical permission checking logic:
     * 1. Check if permission is explicitly denied for user (highest priority)
     * 2. Check if permission is explicitly allowed for user
     * 3. Check role-based permissions (lowest priority)
     */
    private boolean checkUserPermissionHierarchy(UserEntity user, String permissionName) {
        // Priority 1: Check if permission is explicitly denied for this user
        List<String> deniedPermissions = userDeniedPermissionRepository
                .findByUserId(user.getId()).stream()
                .map(p->p.getPermission().getName())
                .toList();
        if (deniedPermissions.contains(permissionName)) {
            return false; // Explicitly denied - highest priority
        }

        // Priority 2: Check if permission is explicitly allowed for this user
        List<String> allowedPermissions = userAllowedPermissionRepository
                .findByUserId(user.getId()).stream()
                .map(p->p.getPermission().getName())
                .toList();
        if (allowedPermissions.contains(permissionName)) {
            return true; // Explicitly allowed - overrides role permissions
        }

        // Priority 3: Check role-based permissions (fallback)
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(permission -> permission.getName()
                    .equalsIgnoreCase(permissionName));
    }

    public boolean currentUserHasPermission(String permissionName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        return userHasPermission(username, permissionName);
    }

    public boolean userHasRole(String username, String roleName) {
        return userRepository.findByEmail(username)
            .map(user -> user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(roleName)))
            .orElse(false);
    }

    public boolean currentUserHasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String username = authentication.getName();
        return userHasRole(username, roleName);
    }

    public List<String> getUserRoles(String username) {
        return userRepository.findByEmail(username)
            .map(user -> user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList()))
            .orElse(List.of());
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    public Long getUserId(String username) {
        return userRepository.findByEmail(username)
                .map(UserEntity::getId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
