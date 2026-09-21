package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.config.security.UserScopeContext;
import biz.craftline.server.config.security.UserScopeContextHolder;
import biz.craftline.server.feature.usermanagement.infra.entity.*;
import biz.craftline.server.feature.usermanagement.infra.repository.UserAllowedPermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserDeniedPermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RBACServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserAllowedPermissionRepository userAllowedPermissionRepository;
    @Mock private UserDeniedPermissionRepository userDeniedPermissionRepository;

    @InjectMocks
    private RBACService rbacService;

    @BeforeEach
    void setUp() {
        UserScopeContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        UserScopeContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasPermission_onRole() {
        PermissionEntity p = new PermissionEntity();
        p.setName("store.read");
        RoleEntity role = new RoleEntity();
        role.setPermissions(Set.of(p));
        assertTrue(rbacService.hasPermission(role, "store.read"));
        assertFalse(rbacService.hasPermission(role, "store.delete"));
        assertFalse(rbacService.hasPermission(new RoleEntity(), "x"));
    }

    @Test
    void userHasPermission_hierarchy() {
        UserEntity user = userWithRolePerm("store.read");
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userDeniedPermissionRepository.findByUserId(1L)).thenReturn(List.of());
        when(userAllowedPermissionRepository.findByUserId(1L)).thenReturn(List.of());

        assertTrue(rbacService.userHasPermission("u@test.com", "store.read"));
        assertFalse(rbacService.userHasPermission("u@test.com", "missing"));
        assertFalse(rbacService.userHasPermission("nobody@test.com", "store.read"));
    }

    @Test
    void userHasPermission_deniedOverridesAllowed() {
        UserEntity user = userWithRolePerm("store.read");
        PermissionEntity denied = new PermissionEntity();
        denied.setName("store.read");
        UserDeniedPermissionEntity deny = new UserDeniedPermissionEntity();
        deny.setPermission(denied);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userDeniedPermissionRepository.findByUserId(1L)).thenReturn(List.of(deny));

        assertFalse(rbacService.userHasPermission("u@test.com", "store.read"));
    }

    @Test
    void getUserPermissions_mergesRoleAndOverrides() {
        UserEntity user = userWithRolePerm("a.read");
        PermissionEntity allowed = new PermissionEntity();
        allowed.setName("b.write");
        UserAllowedPermissionEntity allow = new UserAllowedPermissionEntity();
        allow.setPermission(allowed);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userAllowedPermissionRepository.findByUserId(1L)).thenReturn(List.of(allow));
        when(userDeniedPermissionRepository.findByUserId(1L)).thenReturn(List.of());

        List<String> perms = rbacService.getUserPermissions("u@test.com");
        assertTrue(perms.contains("a.read"));
        assertTrue(perms.contains("b.write"));
    }

    @Test
    void currentUserHasPermission_usesScopeContext() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L).email("u@test.com")
                .roles(List.of("STORE_MANAGER"))
                .permissions(Set.of("store.read"))
                .unrestricted(false)
                .build());
        assertTrue(rbacService.currentUserHasPermission("store.read"));
        assertFalse(rbacService.currentUserHasPermission("admin.only"));
    }

    @Test
    void currentUserHasPermission_fallsBackToSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u@test.com", "pw", List.of()));
        UserEntity user = userWithRolePerm("order.read");
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userDeniedPermissionRepository.findByUserId(1L)).thenReturn(List.of());
        when(userAllowedPermissionRepository.findByUserId(1L)).thenReturn(List.of());

        assertTrue(rbacService.currentUserHasPermission("order.read"));
        assertFalse(rbacService.currentUserHasPermission("missing.perm"));
    }

    @Test
    void userHasRole_and_getUserRoles() {
        RoleEntity role = new RoleEntity();
        role.setName("MANAGER");
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRoles(Set.of(role));
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));

        assertTrue(rbacService.userHasRole("u@test.com", "MANAGER"));
        assertEquals(List.of("MANAGER"), rbacService.getUserRoles("u@test.com"));
    }

    @Test
    void currentUserHasRole_usesScope() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L).email("u@test.com")
                .roles(List.of("BUSINESS_ADMIN"))
                .permissions(Set.of())
                .unrestricted(false)
                .build());
        assertTrue(rbacService.currentUserHasRole("BUSINESS_ADMIN"));
    }

    @Test
    void getCurrentUsername_and_getUserId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u@test.com", "pw"));
        assertEquals("u@test.com", rbacService.getCurrentUsername());

        UserEntity user = new UserEntity();
        user.setId(42L);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        assertEquals(42L, rbacService.getUserId("u@test.com"));
    }

    private static UserEntity userWithRolePerm(String permName) {
        PermissionEntity p = new PermissionEntity();
        p.setName(permName);
        RoleEntity role = new RoleEntity();
        role.setPermissions(Set.of(p));
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setRoles(Set.of(role));
        return user;
    }
}
