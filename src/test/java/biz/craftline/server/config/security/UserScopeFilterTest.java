package biz.craftline.server.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserScopeFilterTest {

    @Mock private UserScopeResolver userScopeResolver;
    @InjectMocks private UserScopeFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        UserScopeContextHolder.clear();
    }

    @Test
    void shouldNotFilter_publicAuthPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_webhook() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/payments/webhook/stripe");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void loadsScopeForAuthenticatedUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of()));

        UserScopeContext scope = UserScopeContext.builder()
                .userId(1L)
                .email("user@test.com")
                .roles(List.of("MANAGER"))
                .permissions(Set.of("order.read"))
                .accessibleStoreIds(List.of(5L))
                .accessibleBusinessIds(List.of(2L))
                .unrestricted(false)
                .build();
        when(userScopeResolver.resolve("user@test.com", 5L, 2L)).thenReturn(scope);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader(UserScopeFilter.HEADER_STORE_ID, "5");
        request.addHeader(UserScopeFilter.HEADER_BUSINESS_ID, "2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertFalse(UserScopeContextHolder.isPresent());
        verify(chain).doFilter(request, response);
        assertInstanceOf(ScopedAuthenticationToken.class,
                SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void returnsForbiddenOnAccessDenied() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of()));
        when(userScopeResolver.resolve(eq("user@test.com"), isNull(), isNull()))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("nope"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void returnsUnauthorizedOnUnexpectedError() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of()));
        when(userScopeResolver.resolve(eq("user@test.com"), isNull(), isNull()))
                .thenThrow(new RuntimeException("db down"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, mock(FilterChain.class));
        assertEquals(401, response.getStatus());
    }

    @Test
    void invalidScopeHeader_forbidden() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of()));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader(UserScopeFilter.HEADER_STORE_ID, "abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, mock(FilterChain.class));
        assertEquals(403, response.getStatus());
    }

    @Test
    void anonymousContinuesWithoutScopeResolve() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        filter.doFilterInternal(new MockHttpServletRequest("GET", "/api/orders"),
                new MockHttpServletResponse(), chain);
        verify(chain).doFilter(any(), any());
        verifyNoInteractions(userScopeResolver);
    }
}
