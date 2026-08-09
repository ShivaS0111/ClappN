package biz.craftline.server.config.security;

import biz.craftline.server.config.RbacSeedData;
import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Role × API-permission matrix enforced through {@link RequirePermissionAspect},
 * using seeded role→permission maps from {@link RbacSeedData}.
 */
@ExtendWith(MockitoExtension.class)
class RolePermissionMatrixAspectTest {

    /** Critical controller permissions sampled across modules. */
    private static final List<String> API_PERMISSIONS = List.of(
            "order.read", "order.create", "order.update",
            "payment.create", "payment.read", "payment.refund",
            "invoice.create", "invoice.read",
            "inventory.read", "inventory.create", "inventory.update", "inventory.delete",
            "customer.read", "customer.create", "customer.delete",
            "user.read", "user.create", "user.delete", "user.permissions",
            "store.read", "store.create", "store.delete", "store.metrics",
            "business.read", "business.create",
            "booking.read", "booking.create", "booking.delete",
            "package.read", "package.create"
    );

    private static final List<String> MATRIX_ROLES = List.of(
            "CASHIER",
            "STORE_MANAGER",
            "INVENTORY_STAFF",
            "SALES_ASSOCIATE",
            "FINANCE_MANAGER",
            "CUSTOMER_SERVICE_REP"
    );

    @Mock private RBACService rbacService;
    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private RequirePermission requirePermission;

    @InjectMocks
    private RequirePermissionAspect aspect;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        UserScopeContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        UserScopeContextHolder.clear();
    }

    static Stream<Arguments> rolePermissionCases() {
        Map<String, List<String>> seed = RbacSeedData.rolePermissionMap();
        return MATRIX_ROLES.stream().flatMap(role -> {
            Set<String> granted = new HashSet<>(seed.getOrDefault(role, List.of()));
            return API_PERMISSIONS.stream()
                    .map(perm -> Arguments.of(role, perm, granted.contains(perm)));
        });
    }

    @ParameterizedTest(name = "{0} × {1} → {2}")
    @MethodSource("rolePermissionCases")
    @DisplayName("Seeded role permissions match aspect allow/deny")
    void aspectEnforcesSeededRoleMatrix(String role, String permission, boolean expectedAllow) throws Throwable {
        Set<String> perms = new HashSet<>(
                RbacSeedData.rolePermissionMap().getOrDefault(role, List.of()));

        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(10L)
                .email(role.toLowerCase() + "@test.com")
                .roles(List.of(role))
                .permissions(perms)
                .accessibleStoreIds(List.of(1L))
                .accessibleBusinessIds(List.of(1L))
                .effectiveStoreIds(List.of(1L))
                .effectiveBusinessIds(List.of(1L))
                .unrestricted(false)
                .build());

        when(rbacService.currentUserHasPermission(anyString())).thenAnswer(inv ->
                UserScopeContextHolder.require().hasPermission(inv.getArgument(0)));
        when(requirePermission.value()).thenReturn(permission);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(role.toLowerCase() + "@test.com", "n/a", List.of()));

        if (expectedAllow) {
            when(joinPoint.proceed()).thenReturn("ok");
            Object result = aspect.checkPermission(joinPoint, requirePermission);
            assertEquals("ok", result);
            verify(joinPoint).proceed();
        } else {
            AccessDeniedException ex = assertThrows(AccessDeniedException.class, this::invokeAspect);
            assertTrue(ex.getMessage().contains(permission));
            verify(joinPoint, never()).proceed();
        }
    }

    private void invokeAspect() {
        try {
            aspect.checkPermission(joinPoint, requirePermission);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
