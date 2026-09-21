package biz.craftline.server.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScopedAuthenticationTokenTest {

    @Test
    void storesRolesStoresAndBusinesses() {
        ScopedAuthenticationToken token = new ScopedAuthenticationToken(
                "user@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                List.of("STORE_MANAGER"),
                List.of(1L, 2L),
                List.of(10L)
        );

        assertEquals("user@test.com", token.getPrincipal());
        assertEquals(List.of("STORE_MANAGER"), token.getRoles());
        assertEquals(List.of(1L, 2L), token.getStoreIds());
        assertEquals(List.of(10L), token.getBusinessIds());
        assertTrue(token.isAuthenticated());
    }

    @Test
    void nullListsBecomeEmpty() {
        ScopedAuthenticationToken token = new ScopedAuthenticationToken(
                "user@test.com",
                "cred",
                List.of(),
                null,
                null,
                null
        );

        assertEquals(List.of(), token.getRoles());
        assertEquals(List.of(), token.getStoreIds());
        assertEquals(List.of(), token.getBusinessIds());
    }
}
