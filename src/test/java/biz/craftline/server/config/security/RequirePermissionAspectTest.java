package biz.craftline.server.config.security;

import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequirePermissionAspectTest {

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

    @Test
    void deniesWhenUnauthenticated() throws Throwable {
        when(requirePermission.value()).thenReturn("order.read");

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> {
                    try {
                        aspect.checkPermission(joinPoint, requirePermission);
                    } catch (AccessDeniedException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                });
        assertTrue(ex.getMessage().contains("Authentication required"));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void deniesWhenPermissionMissing() throws Throwable {
        authenticate("cashier@test.com");
        when(requirePermission.value()).thenReturn("user.delete");
        when(rbacService.currentUserHasPermission("user.delete")).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> invokeAspect());
        assertTrue(ex.getMessage().contains("user.delete"));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void allowsWhenPermissionPresent() throws Throwable {
        authenticate("manager@test.com");
        when(requirePermission.value()).thenReturn("order.read");
        when(rbacService.currentUserHasPermission("order.read")).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.checkPermission(joinPoint, requirePermission);
        assertEquals("ok", result);
        verify(joinPoint).proceed();
    }

    @Test
    void cashierScope_hasOrderCreate_notUserPermissions() throws Throwable {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(5L)
                .email("cashier@test.com")
                .roles(List.of("CASHIER"))
                .permissions(Set.of("order.create", "order.read", "payment.create", "payment.read"))
                .accessibleStoreIds(List.of(1L))
                .accessibleBusinessIds(List.of(1L))
                .effectiveStoreIds(List.of(1L))
                .effectiveBusinessIds(List.of(1L))
                .unrestricted(false)
                .build());

        when(rbacService.currentUserHasPermission(anyString())).thenAnswer(inv ->
                UserScopeContextHolder.require().hasPermission(inv.getArgument(0)));

        authenticate("cashier@test.com");
        when(requirePermission.value()).thenReturn("order.create");
        when(joinPoint.proceed()).thenReturn(null);
        assertDoesNotThrow(this::invokeAspect);

        when(requirePermission.value()).thenReturn("user.permissions");
        assertThrows(AccessDeniedException.class, this::invokeAspect);
    }

    @Test
    void unrestrictedAdmin_allowsAnyPermissionViaAspect() throws Throwable {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L)
                .email("admin@test.com")
                .roles(List.of("SYSTEM_ADMIN"))
                .permissions(Set.of())
                .unrestricted(true)
                .build());

        when(rbacService.currentUserHasPermission(anyString())).thenAnswer(inv ->
                UserScopeContextHolder.require().hasPermission(inv.getArgument(0)));
        authenticate("admin@test.com");
        when(requirePermission.value()).thenReturn("user.delete");
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.checkPermission(joinPoint, requirePermission));
        verify(joinPoint).proceed();
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

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "n/a", List.of()));
    }
}
