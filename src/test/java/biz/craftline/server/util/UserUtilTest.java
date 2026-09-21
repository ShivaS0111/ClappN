package biz.craftline.server.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class UserUtilTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsername_returnsNullWhenNoAuthentication() {
        SecurityContextHolder.clearContext();
        assertNull(UserUtil.getCurrentUsername());
    }

    @Test
    void getCurrentUsername_returnsNullWhenNotAuthenticated() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user@test.com", "pwd");
        // credentials-only constructor leaves authenticated=false
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertNull(UserUtil.getCurrentUsername());
    }

    @Test
    void getCurrentUsername_returnsStringPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@test.com", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertEquals("user@test.com", UserUtil.getCurrentUsername());
    }

    @Test
    void getCurrentUsername_returnsNullForNonStringPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                new Object(), null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertNull(UserUtil.getCurrentUsername());
    }

    @Test
    void requireCurrentUsername_returnsUsername() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "owner@clapp.test", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertEquals("owner@clapp.test", UserUtil.requireCurrentUsername());
    }

    @Test
    void requireCurrentUsername_throwsWhenUnauthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(IllegalStateException.class, UserUtil::requireCurrentUsername);
    }

    @Test
    void requireCurrentUsername_throwsForAnonymousUser() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "anonymousUser", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertThrows(IllegalStateException.class, UserUtil::requireCurrentUsername);
    }
}
