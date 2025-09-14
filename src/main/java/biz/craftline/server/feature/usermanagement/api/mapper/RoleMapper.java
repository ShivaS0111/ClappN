package biz.craftline.server.feature.usermanagement.api.mapper;

import biz.craftline.server.feature.usermanagement.api.dto.RoleDto;
import biz.craftline.server.feature.usermanagement.api.dto.RoleRequest;
import biz.craftline.server.feature.usermanagement.api.dto.RoleResponse;
import biz.craftline.server.feature.usermanagement.domain.model.Role;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;

public class RoleMapper {
    public static RoleDto toDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }
    public static RoleResponse toResponse(Role role) {
        RoleResponse resp = new RoleResponse();
        resp.setId(role.getId());
        resp.setName(role.getName());
        return resp;
    }
    public static Role toDomain(RoleEntity entity) {
        Role role = new Role();
        role.setId(entity.getId());
        role.setName(entity.getName());
        return role;
    }
    public static RoleEntity toEntity(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        return entity;
    }
    public static Role toDomain(RoleRequest req) {
        Role role = new Role();
        role.setName(req.getName());
        return role;
    }
}

