package biz.craftline.server.config.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Records per-request latency, status, and slow-request warnings.
 * Complements Spring Boot's built-in {@code http.server.requests} metrics.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
@Slf4j
public class HttpRequestObservabilityFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;

    @Value("${app.observability.slow-request-ms:2000}")
    private long slowRequestMs;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null
                    && !"anonymousUser".equals(auth.getName())) {
                MDC.put("userEmail", auth.getName());
            }

            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            String path = normalizePath(request.getRequestURI());
            String method = request.getMethod();
            int status = response.getStatus();

            Timer.builder("clappn.http.server.requests")
                    .description("ClappN HTTP request latency")
                    .tag("method", method)
                    .tag("uri", path)
                    .tag("status", String.valueOf(status))
                    .tag("outcome", outcome(status))
                    .register(meterRegistry)
                    .record(tookMs, TimeUnit.MILLISECONDS);

            if (tookMs >= slowRequestMs) {
                log.warn("Slow request method={} path={} status={} durationMs={} requestId={} user={}",
                        method, request.getRequestURI(), status, tookMs,
                        MDC.get("requestId"), MDC.get("userEmail"));
            }

            MDC.remove("userEmail");
        }
    }

    private static String outcome(int status) {
        if (status >= 200 && status < 400) return "SUCCESS";
        if (status >= 400 && status < 500) return "CLIENT_ERROR";
        if (status >= 500) return "SERVER_ERROR";
        return "UNKNOWN";
    }

    /** Collapse numeric path segments to reduce metric cardinality. */
    private static String normalizePath(String uri) {
        if (uri == null || uri.isBlank()) return "unknown";
        String collapsed = uri.replaceAll("/\\d+", "/{id}");
        if (collapsed.length() > 120) {
            return collapsed.substring(0, 120);
        }
        return collapsed;
    }
}
