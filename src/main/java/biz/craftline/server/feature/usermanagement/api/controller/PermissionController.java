package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.api.dto.PermissionRequest;
import biz.craftline.server.feature.usermanagement.api.dto.PermissionResponse;
import biz.craftline.server.feature.usermanagement.api.mapper.PermissionMapper;
import biz.craftline.server.feature.usermanagement.domain.model.Permission;
import biz.craftline.server.feature.usermanagement.domain.service.PermissionService;
import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RBACService rbacService;

    // Helper to get current user's role (assumes role is set in principal)
    private RoleEntity getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof RoleEntity)) {
            throw new SecurityException("Role not found for current user");
        }
        return (RoleEntity) auth.getPrincipal();
    }

    @GetMapping
    public List<PermissionResponse> getAllPermissions() {
        return permissionService.getAllPermissions().stream().map(PermissionMapper::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    public PermissionResponse createPermission(@Valid @RequestBody PermissionRequest request) {
        RoleEntity role = getCurrentUserRole();
        if (!rbacService.hasPermission(role, "PERMISSION_MANAGE")) {
            throw new SecurityException("Forbidden: Insufficient permissions");
        }
        Permission permission = PermissionMapper.toDomain(request);
        Permission created = permissionService.createPermission(permission);
        return PermissionMapper.toResponse(created);
    }

    @DeleteMapping("/{id}")
    public void deletePermission(@PathVariable Long id) {
        RoleEntity role = getCurrentUserRole();
        if (!rbacService.hasPermission(role, "PERMISSION_MANAGE")) {
            throw new SecurityException("Forbidden: Insufficient permissions");
        }
        permissionService.deletePermission(id);
    }
}
