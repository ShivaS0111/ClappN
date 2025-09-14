package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.domain.model.Role;
import biz.craftline.server.feature.usermanagement.api.mapper.RoleMapper;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

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
}
