package biz.craftline.server.config.security;

import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect to enforce @RequirePermission annotation
 * This aspect intercepts method calls annotated with @RequirePermission
 * and checks if the current user has the required permission.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequirePermissionAspect {

    private final RBACService rbacService;

    /**
     * Intercepts methods annotated with @RequirePermission
     */
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated access attempt to method requiring permission: {}", requirePermission.value());
            throw new AccessDeniedException("Authentication required");
        }

        String username = auth.getName();
        String requiredPermission = requirePermission.value();

        // Check if user has the required permission using RBAC service
        boolean hasPermission = rbacService.currentUserHasPermission(requiredPermission);

        if (!hasPermission) {
            log.warn("User '{}' denied access to method requiring permission: {}", username, requiredPermission);
            throw new AccessDeniedException(
                String.format("Access denied. Required permission: %s", requiredPermission)
            );
        }

        log.debug("User '{}' granted access to method with permission: {}", username, requiredPermission);
        return joinPoint.proceed();
    }

    /**
     * Intercepts classes annotated with @RequirePermission
     */
    @Around("@within(requirePermission) && execution(public * *(..))")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        return checkPermission(joinPoint, requirePermission);
    }
}
