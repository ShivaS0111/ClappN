package biz.craftline.server.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthRateLimitFilterTest {

    private AuthRateLimitFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new AuthRateLimitFilter();
        ReflectionTestUtils.setField(filter, "enabled", true);
        ReflectionTestUtils.setField(filter, "maxRequests", 3);
        ReflectionTestUtils.setField(filter, "windowSeconds", 60);
        chain = mock(FilterChain.class);
    }

    @Test
    void allowsUnderLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr("1.2.3.4");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
            assertEquals(200, res.getStatus());
        }
        verify(chain, times(3)).doFilter(any(), any());
    }

    @Test
    void blocksOverLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr("9.9.9.9");
            filter.doFilter(req, new MockHttpServletResponse(), chain);
        }
        MockHttpServletRequest blocked = new MockHttpServletRequest("POST", "/api/auth/login");
        blocked.setRemoteAddr("9.9.9.9");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(blocked, res, chain);
        assertEquals(429, res.getStatus());
        verify(chain, times(3)).doFilter(any(), any());
    }

    @Test
    void skipsNonAuthPaths() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/orders");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        verify(chain, times(1)).doFilter(any(), any());
    }
}
