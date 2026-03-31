package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.domain.model.Role;
import biz.craftline.server.feature.usermanagement.api.mapper.RoleMapper;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll().stream()
            .map(RoleMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name).map(RoleMapper::toDomain);
    }

    public Role createRole(Role role) {
        RoleEntity entity = RoleMapper.toEntity(role);
        RoleEntity saved = roleRepository.save(entity);
        return RoleMapper.toDomain(saved);
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    public List<PermissionEntity> getPermissionsByRoleId(Long roleId) {
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        return role.getPermissions() == null ? List.of() : List.copyOf(role.getPermissions());
    }

    public void addPermissionsToRole(Long roleId, List<Long> permissionIds) {
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        if (role.getPermissions() == null) {
            role.setPermissions(new java.util.HashSet<>());
        }
        List<PermissionEntity> permissions = permissionRepository.findAllById(permissionIds);
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }

    public void removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        if (role.getPermissions() == null) return;
        List<PermissionEntity> permissions = permissionRepository.findAllById(permissionIds);
        role.getPermissions().removeAll(permissions);
        roleRepository.save(role);
    }
}
