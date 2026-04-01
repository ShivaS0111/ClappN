package biz.craftline.server.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtil {

    /**
     * Returns the currently authenticated username (email) from the SecurityContext.
     * Returns null if not authenticated.
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    /**
     * Returns the currently authenticated username, or throws if not authenticated.
     */
    public static String requireCurrentUsername() {
        String username = getCurrentUsername();
        if (username == null || "anonymousUser".equals(username)) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return username;
    }
}
