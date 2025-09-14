package biz.craftline.server.feature.usermanagement.api.mapper;

import biz.craftline.server.feature.usermanagement.api.dto.PermissionDto;
import biz.craftline.server.feature.usermanagement.api.dto.PermissionRequest;
import biz.craftline.server.feature.usermanagement.api.dto.PermissionResponse;
import biz.craftline.server.feature.usermanagement.domain.model.Permission;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;

public class PermissionMapper {
    public static PermissionDto toDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        return dto;
    }
    public static PermissionResponse toResponse(Permission permission) {
        PermissionResponse resp = new PermissionResponse();
        resp.setId(permission.getId());
        resp.setName(permission.getName());
        resp.setDescription(permission.getDescription());
        return resp;
    }
    public static Permission toDomain(PermissionEntity entity) {
        Permission permission = new Permission();
        permission.setId(entity.getId());
        permission.setName(entity.getName());
        permission.setDescription(entity.getDescription());
        return permission;
    }
    public static PermissionEntity toEntity(Permission permission) {
        PermissionEntity entity = new PermissionEntity();
        entity.setId(permission.getId());
        entity.setName(permission.getName());
        entity.setDescription(permission.getDescription());
        return entity;
    }
    public static Permission toDomain(PermissionRequest req) {
        Permission permission = new Permission();
        permission.setName(req.getName());
        permission.setDescription(req.getDescription());
        return permission;
    }
}

