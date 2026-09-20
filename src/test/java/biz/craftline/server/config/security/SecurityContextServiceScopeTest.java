package biz.craftline.server.config.security;

import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityContextServiceScopeTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityContextService securityContextService;

    @BeforeEach
    void setUp() {
        UserScopeContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserScopeContextHolder.clear();
    }

    @Test
    void validateStoreAccess_throwsForForeignStore() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(2L)
                .email("user@test.com")
                .roles(List.of("STORE_MANAGER"))
                .permissions(Set.of("store.read"))
                .accessibleStoreIds(List.of(1L))
                .accessibleBusinessIds(List.of(10L))
                .effectiveStoreIds(List.of(1L))
                .effectiveBusinessIds(List.of(10L))
                .unrestricted(false)
                .build());

        assertDoesNotThrow(() -> securityContextService.validateStoreAccess(1L));
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> securityContextService.validateStoreAccess(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void getAccessibleStoreIds_returnsEffectiveSet() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(2L)
                .email("user@test.com")
                .roles(List.of("STORE_MANAGER"))
                .permissions(Set.of())
                .accessibleStoreIds(List.of(1L, 2L))
                .accessibleBusinessIds(List.of(10L))
                .effectiveStoreIds(List.of(1L))
                .effectiveBusinessIds(List.of(10L))
                .activeStoreId(1L)
                .unrestricted(false)
                .build());

        assertEquals(List.of(1L), securityContextService.getAccessibleStoreIds());
    }

    @Test
    void systemAdmin_getAccessibleStoreIds_isNull() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L)
                .email("admin@test.com")
                .roles(List.of("SYSTEM_ADMIN"))
                .permissions(Set.of())
                .accessibleStoreIds(null)
                .accessibleBusinessIds(null)
                .effectiveStoreIds(null)
                .effectiveBusinessIds(null)
                .unrestricted(true)
                .build());

        assertNull(securityContextService.getAccessibleStoreIds());
        assertDoesNotThrow(() -> securityContextService.validateStoreAccess(12345L));
    }

    @Test
    void getters_fromScopeContext() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(7L)
                .email("mgr@test.com")
                .roles(List.of("STORE_MANAGER"))
                .permissions(Set.of("store.read", "order.read"))
                .accessibleStoreIds(List.of(1L))
                .accessibleBusinessIds(List.of(10L))
                .effectiveStoreIds(List.of(1L))
                .effectiveBusinessIds(List.of(10L))
                .unrestricted(false)
                .build());

        assertEquals("mgr@test.com", securityContextService.getCurrentUsername());
        assertEquals(7L, securityContextService.getCurrentUserId());
        assertEquals(List.of("STORE_MANAGER"), securityContextService.getCurrentUserRoles());
        assertEquals(List.of(1L), securityContextService.getCurrentUserStoreIds());
        assertEquals(List.of(10L), securityContextService.getCurrentUserBusinessIds());
        assertTrue(securityContextService.getCurrentUserPermissions().contains("store.read"));
        assertTrue(securityContextService.hasRole("STORE_MANAGER"));
        assertTrue(securityContextService.hasPermission("order.read"));
        assertFalse(securityContextService.isSystemAdmin());
        assertFalse(securityContextService.isBusinessLevel());
        assertEquals(List.of(10L), securityContextService.getAccessibleBusinessIds());
    }

    @Test
    void validateBusinessAccess_deniesForeignBusiness() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(2L)
                .email("user@test.com")
                .roles(List.of("BUSINESS_ADMIN"))
                .permissions(Set.of())
                .accessibleStoreIds(List.of())
                .accessibleBusinessIds(List.of(10L))
                .effectiveStoreIds(List.of())
                .effectiveBusinessIds(List.of(10L))
                .unrestricted(false)
                .build());

        assertDoesNotThrow(() -> securityContextService.validateBusinessAccess(10L));
        assertThrows(AccessDeniedException.class,
                () -> securityContextService.validateBusinessAccess(99L));
    }

    @Test
    void validateStoreAccessForAll_iterates() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(2L)
                .email("user@test.com")
                .roles(List.of("STORE_MANAGER"))
                .permissions(Set.of())
                .accessibleStoreIds(List.of(1L, 2L))
                .accessibleBusinessIds(List.of(10L))
                .effectiveStoreIds(List.of(1L, 2L))
                .effectiveBusinessIds(List.of(10L))
                .unrestricted(false)
                .build());

        assertDoesNotThrow(() -> securityContextService.validateStoreAccessForAll(List.of(1L, 2L)));
        assertDoesNotThrow(() -> securityContextService.validateStoreAccessForAll(null));
    }

    @Test
    void businessLevelRole_detected() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(3L)
                .email("owner@test.com")
                .roles(List.of("BUSINESS_OWNER"))
                .permissions(Set.of())
                .accessibleBusinessIds(List.of(1L))
                .effectiveBusinessIds(List.of(1L))
                .unrestricted(false)
                .build());
        assertTrue(securityContextService.isBusinessLevel());
    }
}
