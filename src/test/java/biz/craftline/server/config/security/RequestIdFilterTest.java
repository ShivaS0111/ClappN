package biz.craftline.server.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void usesIncomingRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "abc-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) ->
                assertEquals("abc-123", MDC.get(RequestIdFilter.MDC_KEY)));

        assertEquals("abc-123", response.getHeader(RequestIdFilter.HEADER));
        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }

    @Test
    void generatesWhenMissingOrTooLong() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "x".repeat(65));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) -> {
            String id = MDC.get(RequestIdFilter.MDC_KEY);
            assertNotNull(id);
            assertTrue(id.length() <= 64);
            assertFalse(id.contains("-"));
        });

        assertNotNull(response.getHeader(RequestIdFilter.HEADER));
    }

    @Test
    void generatesWhenAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> assertNotNull(MDC.get(RequestIdFilter.MDC_KEY));
        filter.doFilterInternal(request, response, chain);
        assertNotNull(response.getHeader(RequestIdFilter.HEADER));
    }
}
