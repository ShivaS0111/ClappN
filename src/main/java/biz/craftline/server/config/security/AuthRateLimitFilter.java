package biz.craftline.server.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window rate limit for public auth endpoints (login / forgot / reset / register / refresh).
 * Keyed by client IP + path. In-memory (single node); disable via app.rate-limit.enabled=false.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@Slf4j
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/refresh-token",
            "/api/auth/refresh"
    );

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.auth.max-requests:30}")
    private int maxRequests;

    @Value("${app.rate-limit.auth.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        // strip context path if present
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        return !LIMITED_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = normalizePath(request);
        String ip = clientIp(request);
        String key = ip + "|" + path;
        long now = Instant.now().getEpochSecond();
        long windowStart = now - (now % windowSeconds);

        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart != windowStart) {
                return new Window(windowStart, new AtomicInteger(0));
            }
            return existing;
        });

        int count = window.counter.incrementAndGet();
        if (count > maxRequests) {
            log.warn("Auth rate limit exceeded ip={} path={} count={}", ip, path, count);
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Try again later.\",\"statusCode\":429}");
            return;
        }

        // Opportunistic cleanup of stale windows
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(e -> e.getValue().windowStart < windowStart - windowSeconds);
        }

        filterChain.doFilter(request, response);
    }

    private static String normalizePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        return path;
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private record Window(long windowStart, AtomicInteger counter) {}

    /** Test/helper: current window sizes. */
    Map<String, Window> snapshot() {
        return Map.copyOf(windows);
    }
}
