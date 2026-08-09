package biz.craftline.server.config.security;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserScopeContextTest {

    @Test
    void unrestrictedAdmin_canAccessAnyStore() {
        UserScopeContext ctx = UserScopeContext.builder()
                .userId(1L)
                .email("admin@test.com")
                .roles(List.of("SYSTEM_ADMIN"))
                .permissions(Set.of("store.read"))
                .accessibleStoreIds(null)
                .accessibleBusinessIds(null)
                .effectiveStoreIds(null)
                .effectiveBusinessIds(null)
                .unrestricted(true)
                .build();

        assertTrue(ctx.canAccessStore(99L));
        assertTrue(ctx.canAccessBusiness(5L));
        assertNull(ctx.getEffectiveStoreIds());
        assertTrue(ctx.hasPermission("user.delete"));
        assertTrue(ctx.hasPermission("any.permission"));
    }

    @Test
    void unrestrictedAdmin_hasPermissionEvenWithEmptySet() {
        UserScopeContext ctx = UserScopeContext.builder()
                .userId(1L)
                .email("admin@test.com")
                .roles(List.of("SYSTEM_ADMIN"))
                .permissions(Set.of())
                .unrestricted(true)
                .build();

        assertTrue(ctx.hasPermission("order.create"));
    }

    @Test
    void storeUser_cannotAccessForeignStore() {
        UserScopeContext ctx = UserScopeContext.builder()
                .userId(2L)
                .email("cashier@test.com")
                .roles(List.of("STORE_EMPLOYEE"))
                .permissions(Set.of("order.read"))
                .accessibleStoreIds(List.of(10L, 11L))
                .accessibleBusinessIds(List.of(1L))
                .effectiveStoreIds(List.of(10L))
                .effectiveBusinessIds(List.of(1L))
                .activeStoreId(10L)
                .unrestricted(false)
                .build();

        assertTrue(ctx.canAccessStore(10L));
        assertTrue(ctx.canAccessStore(11L));
        assertFalse(ctx.canAccessStore(99L));
        assertEquals(List.of(10L), ctx.getEffectiveStoreIds());
        assertTrue(ctx.hasPermission("order.read"));
        assertFalse(ctx.hasPermission("user.delete"));
    }
}
