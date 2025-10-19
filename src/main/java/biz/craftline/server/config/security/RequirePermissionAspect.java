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

/**
 * AOP Aspect to enforce @RequirePermission annotation
 * This aspect intercepts method calls annotated with @RequirePermission
 * and checks if the current user has the required permission.
 */
@Aspect
// @Component  // COMMENTED OUT - Disabled to fix StackOverflow during authentication
@RequiredArgsConstructor
@Slf4j
public class RequirePermissionAspect {

    private final RBACService rbacService;

    /**
     * Intercepts methods annotated with @RequirePermission - only in application controllers
     * Excludes authentication, security, and service packages to avoid circular dependencies
     */
    @Around("@annotation(requirePermission) && " +
            "execution(* biz.craftline.server.feature..api.controller..*(..))")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        // Always skip during authentication to avoid circular dependency
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                log.debug("Skipping permission check - user not authenticated");
                return joinPoint.proceed();
            }
        } catch (Exception e) {
            log.debug("Exception checking authentication status, allowing call: {}", e.getMessage());
            return joinPoint.proceed();
        }

        String requiredPermission = requirePermission.value();
        if (requiredPermission == null || requiredPermission.isBlank()) {
            return joinPoint.proceed();
        }

        try {
            boolean allowed = rbacService.currentUserHasPermission(requiredPermission);
            if (!allowed) {
                log.warn("Access denied for permission '{}'", requiredPermission);
                throw new AccessDeniedException("Access denied. Required permission: " + requiredPermission);
            }

            log.debug("Permission '{}' granted by AOP for current user", requiredPermission);
            return joinPoint.proceed();
        } catch (Exception e) {
            // If there's any issue checking permissions, log and allow
            log.warn("Exception during permission check, allowing call: {}", e.getMessage());
            return joinPoint.proceed();
        }
    }

    /**
     * Intercepts classes annotated with @RequirePermission - only controllers
     */
    @Around("@within(requirePermission) && " +
            "execution(public * biz.craftline.server.feature..api.controller..*(..))")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        return checkPermission(joinPoint, requirePermission);
    }
}
