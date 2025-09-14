package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.api.dto.RoleRequest;
import biz.craftline.server.feature.usermanagement.api.dto.RoleResponse;
import biz.craftline.server.feature.usermanagement.api.mapper.RoleMapper;
import biz.craftline.server.feature.usermanagement.domain.model.Role;
import biz.craftline.server.feature.usermanagement.domain.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
