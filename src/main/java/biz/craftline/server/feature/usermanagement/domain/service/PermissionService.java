package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.domain.model.Permission;
import biz.craftline.server.feature.usermanagement.api.mapper.PermissionMapper;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll().stream()
            .map(PermissionMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Optional<Permission> getPermissionByName(String name) {
        return permissionRepository.findByName(name).map(PermissionMapper::toDomain);
    }

    public Permission createPermission(Permission permission) {
        PermissionEntity entity = PermissionMapper.toEntity(permission);
        PermissionEntity saved = permissionRepository.save(entity);
        return PermissionMapper.toDomain(saved);
    }

    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }
}
