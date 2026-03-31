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
        dto.setDescription(permission.getDescription() != null ? permission.getDescription() : "");
        dto.setCategory(permission.getCategory() != null ? permission.getCategory() : "general");
        dto.setType(permission.getType() != null ? permission.getType() : "standard");
        dto.setLevel(permission.getLevel() != null ? permission.getLevel() : "basic");
        return dto;
    }
    public static PermissionResponse toResponse(Permission permission) {
        PermissionResponse resp = new PermissionResponse();
        resp.setId(permission.getId());
        resp.setName(permission.getName());
        resp.setDescription(permission.getDescription() != null ? permission.getDescription() : "");
        resp.setCategory(permission.getCategory() != null ? permission.getCategory() : "general");
        resp.setType(permission.getType() != null ? permission.getType() : "standard");
        resp.setLevel(permission.getLevel() != null ? permission.getLevel() : "basic");
        return resp;
    }
    public static Permission toDomain(PermissionEntity entity) {
        Permission permission = new Permission();
        permission.setId(entity.getId());
        permission.setName(entity.getName());
        permission.setDescription(entity.getDescription() != null ? entity.getDescription() : "");
        permission.setCategory(entity.getCategory() != null ? entity.getCategory() : "general");
        permission.setType(entity.getType() != null ? entity.getType() : "standard");
        permission.setLevel(entity.getLevel() != null ? entity.getLevel() : "basic");
        return permission;
    }
    public static PermissionEntity toEntity(Permission permission) {
        PermissionEntity entity = new PermissionEntity();
        entity.setId(permission.getId());
        entity.setName(permission.getName());
        entity.setDescription(permission.getDescription() != null ? permission.getDescription() : "");
        entity.setCategory(permission.getCategory() != null ? permission.getCategory() : "general");
        entity.setType(permission.getType() != null ? permission.getType() : "standard");
        entity.setLevel(permission.getLevel() != null ? permission.getLevel() : "basic");
        return entity;
    }
    public static Permission toDomain(PermissionRequest req) {
        Permission permission = new Permission();
        permission.setName(req.getName());
        permission.setDescription(req.getDescription() != null ? req.getDescription() : "");
        permission.setCategory(req.getCategory() != null ? req.getCategory() : "general");
        permission.setType(req.getType() != null ? req.getType() : "standard");
        permission.setLevel(req.getLevel() != null ? req.getLevel() : "basic");
        return permission;
    }
}

