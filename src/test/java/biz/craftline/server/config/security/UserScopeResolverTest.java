package biz.craftline.server.config.security;

import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserAllowedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserDeniedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.UserAllowedPermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserDeniedPermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserScopeResolverTest {

    @Mock private UserRepository userRepository;
    @Mock private MembershipRepository membershipRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private UserAllowedPermissionRepository allowedPermissionRepository;
    @Mock private UserDeniedPermissionRepository deniedPermissionRepository;

    @InjectMocks
    private UserScopeResolver resolver;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(5L);
        user.setEmail("user@test.com");
        user.setRoles(new HashSet<>());
    }

    private RoleEntity role(String name, String... permissions) {
        RoleEntity role = new RoleEntity();
        role.setName(name);
        Set<PermissionEntity> perms = new HashSet<>();
        for (String p : permissions) {
            PermissionEntity pe = new PermissionEntity(p);
            perms.add(pe);
        }
        role.setPermissions(perms);
        return role;
    }

    @Test
    void resolve_throwsWhenUserNotFound() {
        when(userRepository.findByEmailWithRolesAndPermissions("missing@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> resolver.resolve("missing@test.com", null, null));
    }

    @Test
    void systemAdmin_isUnrestricted() {
        user.getRoles().add(role("SYSTEM_ADMIN", "store.read"));
        when(userRepository.findByEmailWithRolesAndPermissions("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(5L, MembershipEntity.STATUS_ACTIVE)).thenReturn(List.of());
        when(allowedPermissionRepository.findByUserId(5L)).thenReturn(List.of());
        when(deniedPermissionRepository.findByUserId(5L)).thenReturn(List.of());

        UserScopeContext ctx = resolver.resolve("user@test.com", 999L, 888L);

        assertTrue(ctx.isUnrestricted());
        assertNull(ctx.getAccessibleStoreIds());
        assertNull(ctx.getAccessibleBusinessIds());
        assertNull(ctx.getEffectiveStoreIds());
        assertNull(ctx.getEffectiveBusinessIds());
        assertTrue(ctx.getRoles().contains("SYSTEM_ADMIN"));
        assertTrue(ctx.getPermissions().contains("store.read"));
    }

    @Test
    void membership_withStoreScopes() {
        RoleEntity storeMgr = role("STORE_MANAGER", "store.read");
        MembershipEntity membership = MembershipEntity.builder()
                .id(1L)
                .userId(5L)
                .businessId(10L)
                .status(MembershipEntity.STATUS_ACTIVE)
                .roles(Set.of(storeMgr))
                .storeScopes(Set.of(1L, 2L))
                .build();

        when(userRepository.findByEmailWithRolesAndPermissions("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(5L, MembershipEntity.STATUS_ACTIVE))
                .thenReturn(List.of(membership));
        when(allowedPermissionRepository.findByUserId(5L)).thenReturn(List.of());
        when(deniedPermissionRepository.findByUserId(5L)).thenReturn(List.of());

        UserScopeContext ctx = resolver.resolve("user@test.com", 1L, 10L);

        assertFalse(ctx.isUnrestricted());
        assertEquals(List.of(10L), ctx.getAccessibleBusinessIds());
        assertTrue(ctx.getAccessibleStoreIds().containsAll(List.of(1L, 2L)));
        assertEquals(List.of(1L), ctx.getEffectiveStoreIds());
        assertEquals(List.of(10L), ctx.getEffectiveBusinessIds());
        assertTrue(ctx.getRoles().contains("STORE_MANAGER"));
        assertTrue(ctx.getPermissions().contains("store.read"));
    }

    @Test
    void businessLevel_emptyScopes_loadsAllStores() {
        RoleEntity owner = role("BUSINESS_OWNER", "business.read");
        MembershipEntity membership = MembershipEntity.builder()
                .id(1L)
                .userId(5L)
                .businessId(10L)
                .status(MembershipEntity.STATUS_ACTIVE)
                .roles(Set.of(owner))
                .storeScopes(Set.of())
                .build();

        StoreEntity s1 = StoreEntity.builder().id(11L).build();
        StoreEntity s2 = StoreEntity.builder().id(12L).build();

        when(userRepository.findByEmailWithRolesAndPermissions("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(5L, MembershipEntity.STATUS_ACTIVE))
                .thenReturn(List.of(membership));
        when(storeRepository.findByBusinessId(10L)).thenReturn(List.of(s1, s2));
        when(allowedPermissionRepository.findByUserId(5L)).thenReturn(List.of());
        when(deniedPermissionRepository.findByUserId(5L)).thenReturn(List.of());

        UserScopeContext ctx = resolver.resolve("user@test.com", null, null);

        assertEquals(Set.of(11L, 12L), Set.copyOf(ctx.getAccessibleStoreIds()));
        assertEquals(List.of(10L), ctx.getAccessibleBusinessIds());
        assertTrue(ctx.getRoles().contains("BUSINESS_OWNER"));
    }

    @Test
    void allowedAndDeniedPermissionOverlays() {
        RoleEntity storeMgr = role("STORE_MANAGER", "store.read", "order.read");
        MembershipEntity membership = MembershipEntity.builder()
                .id(1L)
                .userId(5L)
                .businessId(10L)
                .status(MembershipEntity.STATUS_ACTIVE)
                .roles(Set.of(storeMgr))
                .storeScopes(Set.of(1L))
                .build();

        PermissionEntity extra = new PermissionEntity("customer.create");
        UserAllowedPermissionEntity allowed = new UserAllowedPermissionEntity(user, extra);

        PermissionEntity deny = new PermissionEntity("order.read");
        UserDeniedPermissionEntity denied = new UserDeniedPermissionEntity(user, deny);

        when(userRepository.findByEmailWithRolesAndPermissions("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(5L, MembershipEntity.STATUS_ACTIVE))
                .thenReturn(List.of(membership));
        when(allowedPermissionRepository.findByUserId(5L)).thenReturn(List.of(allowed));
        when(deniedPermissionRepository.findByUserId(5L)).thenReturn(List.of(denied));

        UserScopeContext ctx = resolver.resolve("user@test.com", null, null);

        assertTrue(ctx.getPermissions().contains("store.read"));
        assertTrue(ctx.getPermissions().contains("customer.create"));
        assertFalse(ctx.getPermissions().contains("order.read"));
    }

    @Test
    void activeBusiness_withoutMembership_throws() {
        when(userRepository.findByEmailWithRolesAndPermissions("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(5L, MembershipEntity.STATUS_ACTIVE)).thenReturn(List.of());

        assertThrows(AccessDeniedException.class,
                () -> resolver.resolve("user@test.com", null, 99L));
    }

    @Test
    void activeStore_notInScope_throws() {
        RoleEntity storeMgr = role("STORE_MANAGER", "store.read");
        MembershipEntity membership = MembershipEntity.builder()
                .id(1L)
                .userId(5L)
                .businessId(10L)
                .status(MembershipEntity.STATUS_ACTIVE)
                .roles(Set.of(storeMgr))
                .storeScopes(Set.of(1L))
                .build();

        when(userRepository.findByEmailWithRolesAndPermissions("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(5L, MembershipEntity.STATUS_ACTIVE))
                .thenReturn(List.of(membership));
        when(allowedPermissionRepository.findByUserId(5L)).thenReturn(List.of());
        when(deniedPermissionRepository.findByUserId(5L)).thenReturn(List.of());

        assertThrows(AccessDeniedException.class,
                () -> resolver.resolve("user@test.com", 99L, null));
    }

    @Test
    void activeBusinessId_filtersEffectiveStores() {
        RoleEntity storeMgr = role("STORE_MANAGER", "store.read");
        MembershipEntity membership = MembershipEntity.builder()
                .id(1L)
                .userId(5L)
                .businessId(10L)
                .status(MembershipEntity.STATUS_ACTIVE)
                .roles(Set.of(storeMgr))
                .storeScopes(Set.of(1L, 2L, 3L))
                .build();

        StoreEntity s1 = StoreEntity.builder().id(1L).build();
        StoreEntity s2 = StoreEntity.builder().id(2L).build();

        when(userRepository.findByEmailWithRolesAndPermissions("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(5L, MembershipEntity.STATUS_ACTIVE))
                .thenReturn(List.of(membership));
        when(storeRepository.findByBusinessId(eq(10L))).thenReturn(List.of(s1, s2));
        when(allowedPermissionRepository.findByUserId(5L)).thenReturn(List.of());
        when(deniedPermissionRepository.findByUserId(5L)).thenReturn(List.of());

        UserScopeContext ctx = resolver.resolve("user@test.com", null, 10L);

        assertEquals(Set.of(1L, 2L), Set.copyOf(ctx.getEffectiveStoreIds()));
        assertEquals(List.of(10L), ctx.getEffectiveBusinessIds());
    }
}
