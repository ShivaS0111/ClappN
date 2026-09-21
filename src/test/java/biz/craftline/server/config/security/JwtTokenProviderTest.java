package biz.craftline.server.config.security;

import biz.craftline.server.feature.usermanagement.domain.model.TokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() throws Exception {
        provider = new JwtTokenProvider();
        // HMAC key must be >= 256 bits
        setField(provider, "jwtSecret", "test-secret-key-that-is-long-enough-for-hs256!!");
        setField(provider, "jwtExpirationInMs", 3_600_000L);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = JwtTokenProvider.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void generateToken_andExtractUsername() {
        UserDetails user = User.withUsername("alice@test.com").password("x").roles("USER").build();
        String token = provider.generateToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("alice@test.com", provider.getUsernameFromToken(token));
    }

    @Test
    void generateTokenWithClaims_roundTripsClaims() {
        TokenInfo info = provider.generateTokenWithClaims(
                "bob@test.com",
                List.of("store.read", "order.create"),
                List.of("STORE_MANAGER"),
                List.of(1L, 2L),
                List.of(99L)
        );

        assertNotNull(info.getToken());
        assertNotNull(info.getRefreshToken());
        assertNotNull(info.getTokenExpiry());

        String jwt = info.getToken();
        assertEquals("bob@test.com", provider.getUsernameFromToken(jwt));
        assertEquals(List.of("store.read", "order.create"), provider.getPermissionsFromToken(jwt));
        assertEquals(List.of("STORE_MANAGER"), provider.getRolesFromToken(jwt));
        assertEquals(List.of(1L, 2L), provider.getStoreIdsFromToken(jwt));
        assertEquals(List.of(99L), provider.getBusinessIdsFromToken(jwt));
    }

    @Test
    void generateTokenWithPermissions_delegates() {
        TokenInfo info = provider.generateTokenWithPermissions("carol@test.com", List.of("brand.read"));
        assertEquals(List.of("brand.read"), provider.getPermissionsFromToken(info.getToken()));
        assertEquals(List.of(), provider.getRolesFromToken(info.getToken()));
        assertEquals(List.of(), provider.getStoreIdsFromToken(info.getToken()));
        assertEquals(List.of(), provider.getBusinessIdsFromToken(info.getToken()));
    }

    @Test
    void validateToken_returnsFalseForMalformed() {
        assertFalse(provider.validateToken("not-a-jwt"));
    }

    @Test
    void validateToken_returnsFalseForEmpty() {
        assertFalse(provider.validateToken(""));
    }

    @Test
    void rolesAndIds_defaultEmptyWhenMissingClaims() {
        // generateToken (no claims) still parses as empty lists for roles/stores/businesses
        UserDetails user = User.withUsername("dave@test.com").password("x").roles("USER").build();
        String token = provider.generateToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        assertEquals(List.of(), provider.getRolesFromToken(token));
        assertEquals(List.of(), provider.getStoreIdsFromToken(token));
        assertEquals(List.of(), provider.getBusinessIdsFromToken(token));
    }
}
