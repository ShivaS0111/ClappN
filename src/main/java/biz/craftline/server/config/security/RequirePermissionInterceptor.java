package biz.craftline.server.config.security;

import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces {@link RequirePermission} before controller argument resolution / {@code @Valid}.
 * Spring AOP around the controller method runs too late (after Bean Validation), which can
 * return 400 instead of 403 for unauthorized callers with invalid bodies.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequirePermissionInterceptor implements HandlerInterceptor {

    private final RBACService rbacService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (requirePermission == null) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            log.warn("Unauthenticated access attempt requiring permission: {}", requirePermission.value());
            throw new AccessDeniedException("Authentication required");
        }

        String required = requirePermission.value();
        if (!rbacService.currentUserHasPermission(required)) {
            log.warn("User '{}' denied access requiring permission: {}", auth.getName(), required);
            throw new AccessDeniedException(
                    String.format("Access denied. Required permission: %s", required));
        }

        return true;
    }
}
