package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.usermanagement.api.dto.PermissionRequest;
import biz.craftline.server.feature.usermanagement.api.dto.PermissionResponse;
import biz.craftline.server.feature.usermanagement.api.mapper.PermissionMapper;
import biz.craftline.server.feature.usermanagement.domain.model.Permission;
import biz.craftline.server.feature.usermanagement.domain.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;


    @GetMapping
    @RequirePermission("user.permissions")
    public List<PermissionResponse> getAllPermissions() {
        return permissionService.getAllPermissions().stream().map(PermissionMapper::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    @RequirePermission("user.permissions")
    public PermissionResponse createPermission(@Valid @RequestBody PermissionRequest request) {
        Permission permission = PermissionMapper.toDomain(request);
        Permission created = permissionService.createPermission(permission);
        return PermissionMapper.toResponse(created);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user.permissions")
    public void deletePermission(@PathVariable Long id) {
        System.out.println("Attempting to delete permission with ID: " + id);
        permissionService.deletePermission(id);
    }
}
