package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserAllowedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserDeniedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.UserAllowedPermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserDeniedPermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RBACServicePermissionHierarchyTest {

    @Mock private UserRepository userRepository;
    @Mock private UserAllowedPermissionRepository userAllowedPermissionRepository;
    @Mock private UserDeniedPermissionRepository userDeniedPermissionRepository;

    @InjectMocks
    private RBACService rbacService;

    @Test
    void rolePermission_grantsAccess() {
        UserEntity user = userWithRolePerms("order.read");
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userDeniedPermissionRepository.findByUserId(1L)).thenReturn(List.of());
        when(userAllowedPermissionRepository.findByUserId(1L)).thenReturn(List.of());

        assertTrue(rbacService.userHasPermission("u@test.com", "order.read"));
        assertFalse(rbacService.userHasPermission("u@test.com", "user.delete"));
    }

    @Test
    void userDenied_overridesRolePermission() {
        UserEntity user = userWithRolePerms("order.read");
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userDeniedPermissionRepository.findByUserId(1L)).thenReturn(List.of(
                denied(user, "order.read")));

        assertFalse(rbacService.userHasPermission("u@test.com", "order.read"));
    }

    @Test
    void userAllowed_grantsWhenRoleLacksPermission() {
        UserEntity user = userWithRolePerms("order.read");
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userDeniedPermissionRepository.findByUserId(1L)).thenReturn(List.of());
        when(userAllowedPermissionRepository.findByUserId(1L)).thenReturn(List.of(
                allowed(user, "invoice.create")));

        assertTrue(rbacService.userHasPermission("u@test.com", "invoice.create"));
    }

    @Test
    void deniedBeatsAllowed() {
        UserEntity user = userWithRolePerms();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userDeniedPermissionRepository.findByUserId(1L)).thenReturn(List.of(
                denied(user, "invoice.create")));

        assertFalse(rbacService.userHasPermission("u@test.com", "invoice.create"));
    }

    private UserEntity userWithRolePerms(String... perms) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("u@test.com");
        RoleEntity role = new RoleEntity();
        role.setName("CASHIER");
        Set<PermissionEntity> permissions = new HashSet<>();
        for (String p : perms) {
            permissions.add(new PermissionEntity(p));
        }
        role.setPermissions(permissions);
        user.setRoles(Set.of(role));
        return user;
    }

    private UserDeniedPermissionEntity denied(UserEntity user, String name) {
        UserDeniedPermissionEntity e = new UserDeniedPermissionEntity();
        e.setUser(user);
        e.setPermission(new PermissionEntity(name));
        return e;
    }

    private UserAllowedPermissionEntity allowed(UserEntity user, String name) {
        UserAllowedPermissionEntity e = new UserAllowedPermissionEntity();
        e.setUser(user);
        e.setPermission(new PermissionEntity(name));
        return e;
    }
}
