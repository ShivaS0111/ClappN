package biz.craftline.server.feature.usermanagement.api.mapper;

import biz.craftline.server.feature.usermanagement.api.dto.UserDto;
import biz.craftline.server.feature.usermanagement.api.dto.UserCreateRequest;
import biz.craftline.server.feature.usermanagement.api.dto.UserUpdateRequest;
import biz.craftline.server.feature.usermanagement.domain.model.AuthUser;
import biz.craftline.server.feature.usermanagement.domain.model.Permission;
import biz.craftline.server.feature.usermanagement.domain.model.Role;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserAllowedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;

import java.util.List;

public class UserMapper {
    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        return dto;
    }

    public static User toDomain(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setEnabled(entity.isEnabled());
        return user;
    }

    public static AuthUser toAuthUser(UserEntity entity) {
        AuthUser user = new AuthUser();
        user.setId(entity.getId());
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setEnabled(entity.isEnabled());

        List<Role> roles = entity.getRoles().stream().map(RoleMapper::toDomain).toList();
        user.setRoles(roles.stream().map(Role::getName).toList());

        List<String> permissions = entity.getRoles().stream().map(RoleEntity::getPermissions)
                .flatMap(perms -> perms.stream().map(PermissionEntity::getName)).toList();
        user.setPermissions(permissions);

        user.setAllowedPermissions(entity.getAllowedPermissions().stream().map(p->p.getPermission().getName()).toList());
        user.setDeniedPermissions(entity.getDeniedPermissions().stream().map(p->p.getPermission().getName()).toList());

        return user;
    }

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setEnabled(user.isEnabled());
        return entity;
    }

    public static User toDomain(UserCreateRequest req) {
        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setEnabled(true);
        return user;
    }

    public static void updateDomain(User user, UserUpdateRequest req) {
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setEnabled(req.isEnabled());
    }
}

