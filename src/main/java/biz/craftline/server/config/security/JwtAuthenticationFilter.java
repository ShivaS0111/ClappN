package biz.craftline.server.config.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates the caller from JWT identity (subject/email) only.
 * Permissions and store/business scope are loaded per-request by {@link UserScopeFilter}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        log.trace("[3-JwtAuthenticationFilter] ENTER: path={}", path);
        
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                log.trace("[3-JwtAuthenticationFilter] Token found, validating...");
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    log.trace("[3-JwtAuthenticationFilter] Token valid for user: {}", username);

                    // Identity only — authorities filled by UserScopeFilter from DB
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, List.of());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.trace("[3-JwtAuthenticationFilter] SET: Authentication context set for {}", username);
                } else {
                    log.warn("[3-JwtAuthenticationFilter] Token validation FAILED");
                }
            } else {
                log.trace("[3-JwtAuthenticationFilter] No Bearer token found in Authorization header");
            }
        } catch (Exception ex) {
            log.error("[3-JwtAuthenticationFilter] ERROR: Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
