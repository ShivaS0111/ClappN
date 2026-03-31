package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.api.dto.PermissionIdsRequest;
import biz.craftline.server.feature.usermanagement.api.dto.PermissionResponse;
import biz.craftline.server.feature.usermanagement.api.dto.RoleRequest;
import biz.craftline.server.feature.usermanagement.api.dto.RoleResponse;
import biz.craftline.server.feature.usermanagement.api.mapper.PermissionMapper;
import biz.craftline.server.feature.usermanagement.api.mapper.RoleMapper;
import biz.craftline.server.feature.usermanagement.domain.model.Role;
import biz.craftline.server.feature.usermanagement.domain.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles().stream().map(RoleMapper::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    public RoleResponse createRole(@RequestBody RoleRequest request) {
        Role role = RoleMapper.toDomain(request);
        Role created = roleService.createRole(role);
        return RoleMapper.toResponse(created);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }

    @GetMapping("/{roleId}/permissions")
    public List<PermissionResponse> getPermissionsByRole(@PathVariable Long roleId) {
        return roleService.getPermissionsByRoleId(roleId).stream()
                .map(PermissionMapper::toDomain)
                .map(PermissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<Void> addPermissionsToRole(@PathVariable Long roleId, @RequestBody PermissionIdsRequest request) {
        roleService.addPermissionsToRole(roleId, request.getPermissionIds());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roleId}/permissions/delete")
    public ResponseEntity<Void> removePermissionsFromRole(@PathVariable Long roleId, @RequestBody PermissionIdsRequest request) {
        roleService.removePermissionsFromRole(roleId, request.getPermissionIds());
        return ResponseEntity.ok().build();
    }
}
