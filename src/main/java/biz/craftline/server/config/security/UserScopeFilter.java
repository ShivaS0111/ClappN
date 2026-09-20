package biz.craftline.server.config.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * After JWT authentication, loads permissions and accessible stores/businesses from DB
 * once per request into {@link UserScopeContextHolder}.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 50)
@RequiredArgsConstructor
@Slf4j
public class UserScopeFilter extends OncePerRequestFilter {

    public static final String HEADER_STORE_ID = "X-Store-Id";
    public static final String HEADER_BUSINESS_ID = "X-Business-Id";

    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    );

    private final UserScopeResolver userScopeResolver;

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Strip context path if present
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return PUBLIC_AUTH_PATHS.contains(path)
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/actuator/")
                || path.startsWith("/api/payments/webhook/")
                || path.startsWith("/payment/callback");
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && auth.getPrincipal() != null
                    && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {

                String email = auth.getName();
                Long activeStoreId = parseLongHeader(request.getHeader(HEADER_STORE_ID));
                Long activeBusinessId = parseLongHeader(request.getHeader(HEADER_BUSINESS_ID));

                UserScopeContext scope = userScopeResolver.resolve(email, activeStoreId, activeBusinessId);
                UserScopeContextHolder.set(scope);

                List<SimpleGrantedAuthority> authorities = scope.getPermissions().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                ScopedAuthenticationToken scopedAuth = new ScopedAuthenticationToken(
                        email,
                        null,
                        authorities,
                        scope.getRoles(),
                        scope.getAccessibleStoreIds() != null ? scope.getAccessibleStoreIds() : List.of(),
                        scope.getAccessibleBusinessIds() != null ? scope.getAccessibleBusinessIds() : List.of()
                );
                scopedAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(scopedAuth);
            }

            filterChain.doFilter(request, response);
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            log.warn("Scope access denied: {}", ex.getMessage());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"" + sanitize(ex.getMessage()) + "\",\"status\":403}");
        } catch (Exception ex) {
            log.error("Failed to resolve user scope for request", ex);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Unable to resolve user scope\",\"status\":401}");
        } finally {
            UserScopeContextHolder.clear();
        }
    }

    private Long parseLongHeader(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid scope header value: " + value);
        }
    }

    private String sanitize(String message) {
        if (message == null) return "Access denied";
        return message.replace("\"", "'");
    }
}
