package biz.craftline.server.config.security;

import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequirePermissionInterceptorTest {

    @Mock private RBACService rbacService;
    @InjectMocks private RequirePermissionInterceptor interceptor;

    static class SecuredController {
        @RequirePermission("order.read")
        public void secured() {}

        public void open() {}
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsNonHandlerMethod() {
        assertTrue(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
    }

    @Test
    void skipsWhenNoAnnotation() throws Exception {
        HandlerMethod hm = new HandlerMethod(new SecuredController(), SecuredController.class.getMethod("open"));
        assertTrue(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), hm));
        verifyNoInteractions(rbacService);
    }

    @Test
    void deniesUnauthenticated() throws Exception {
        HandlerMethod hm = new HandlerMethod(new SecuredController(), SecuredController.class.getMethod("secured"));
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), hm));
        assertTrue(ex.getMessage().contains("Authentication required"));
    }

    @Test
    void deniesMissingPermission() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u@test.com", null, List.of()));
        when(rbacService.currentUserHasPermission("order.read")).thenReturn(false);
        HandlerMethod hm = new HandlerMethod(new SecuredController(), SecuredController.class.getMethod("secured"));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), hm));
        assertTrue(ex.getMessage().contains("order.read"));
    }

    @Test
    void deniesAnonymousAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of()));
        HandlerMethod hm = new HandlerMethod(new SecuredController(), SecuredController.class.getMethod("secured"));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), hm));
        assertTrue(ex.getMessage().contains("Authentication required"));
    }

    @Test
    void allowsWhenPermissionPresent() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u@test.com", null, List.of()));
        when(rbacService.currentUserHasPermission("order.read")).thenReturn(true);
        HandlerMethod hm = new HandlerMethod(new SecuredController(), SecuredController.class.getMethod("secured"));

        assertTrue(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), hm));
    }
}
