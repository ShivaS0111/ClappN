package biz.craftline.server.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                List<String> permissions = tokenProvider.getPermissionsFromToken(jwt);

                // Convert permissions to Spring Security authorities (with normalization)
                Set<String> authorityNames = new HashSet<>();
                if (permissions != null) {
                    for (String p : permissions) {
                        if (!StringUtils.hasText(p)) continue;
                        String perm = p.trim();

                        // keep original
                        authorityNames.add(perm);

                        // generic normalized form: RESOURCE_ACTION (user.create -> USER_CREATE)
                        authorityNames.add(perm.toUpperCase().replace('.', '_'));

                        // if pattern like resource.action, also add ACTION_RESOURCE (CREATE_USER)
                        if (perm.contains(".")) {
                            String[] parts = perm.split("\\.");
                            if (parts.length == 2) {
                                String resource = parts[0].toUpperCase();
                                String action = parts[1].toUpperCase();
                                authorityNames.add(action + "_" + resource);
                                authorityNames.add(resource + "_" + action);

                                // add VIEW_RESOURCE and VIEW_RESOURCES for read actions to match existing checks like VIEW_USERS
                                if ("READ".equals(action)) {
                                    authorityNames.add("VIEW_" + resource);
                                    authorityNames.add("VIEW_" + resource + "S");
                                }

                                // special-case permissions action -> MANAGEMENT/ADMIN synonyms
                                if ("PERMISSIONS".equals(action) || "MANAGE".equals(action) || "MANAGE_PERMISSIONS".equals(action)) {
                                    authorityNames.add("MANAGE_" + resource + "_PERMISSIONS");
                                    authorityNames.add(resource.toUpperCase() + "_MANAGEMENT");
                                }
                            }
                        }

                        // wildcard/all
                        if ("*".equals(perm) || "ALL".equalsIgnoreCase(perm)) {
                            authorityNames.add("ADMIN");
                            authorityNames.add("ALL");
                            authorityNames.add("*");
                        }
                    }
                }

                List<SimpleGrantedAuthority> authorities = authorityNames.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set Security context for user: {}, permissions: {}", username, authorityNames);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
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
