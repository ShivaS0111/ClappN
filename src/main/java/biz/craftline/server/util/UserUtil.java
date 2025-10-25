package biz.craftline.server.util;

import io.jsonwebtoken.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserUtil {

    public static Long getCurrentUserId() {
        /*Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getId();
        } else if (principal instanceof Jwt) {
            return ((Jwt) principal).getClaim("user_id");
        }*/
        return 1L;
    }
}
