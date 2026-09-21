package biz.craftline.server.config.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestObservabilityFilterTest {

    private SimpleMeterRegistry registry;
    private HttpRequestObservabilityFilter filter;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        filter = new HttpRequestObservabilityFilter(registry);
        ReflectionTestUtils.setField(filter, "slowRequestMs", 2000L);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter_actuator() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void recordsTimerAndNormalizesPath() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of()));

        FilterChain chain = (req, res) -> { /* no-op */ };
        filter.doFilterInternal(request, response, chain);

        assertNotNull(registry.find("clappn.http.server.requests")
                .tag("method", "GET")
                .tag("uri", "/api/orders/{id}")
                .tag("status", "200")
                .tag("outcome", "SUCCESS")
                .timer());
        assertEquals(1, registry.find("clappn.http.server.requests").timers().size());
    }

    @Test
    void shouldNotFilter_swaggerAndApiDocs() {
        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/swagger-ui/index.html")));
        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/v3/api-docs")));
    }

    @Test
    void recordsServerErrorOutcome() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/fail");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(503);
        filter.doFilterInternal(request, response, (req, res) -> {});

        assertNotNull(registry.find("clappn.http.server.requests")
                .tag("outcome", "SERVER_ERROR")
                .timer());
    }

    @Test
    void recordsUnknownOutcomeForNonStandardStatus() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/redirect");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(102);
        filter.doFilterInternal(request, response, (req, res) -> {});

        assertNotNull(registry.find("clappn.http.server.requests")
                .tag("outcome", "UNKNOWN")
                .timer());
    }

    @Test
    void recordsClientErrorOutcome() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/x");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);
        filter.doFilterInternal(request, response, (req, res) -> {});

        assertNotNull(registry.find("clappn.http.server.requests")
                .tag("outcome", "CLIENT_ERROR")
                .timer());
    }
}
