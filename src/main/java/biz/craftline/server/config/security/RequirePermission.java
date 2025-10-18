package biz.craftline.server.config.security;

import java.lang.annotation.*;

/**
 * Custom annotation for method-level permission checking.
 * This annotation marks methods that require specific permissions.
 * Can be used in conjunction with AOP or Spring Security method security.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * The permission name required to access the annotated method
     */
    String value();

    /**
     * Optional description of what this permission allows
     */
    String description() default "";

    /**
     * Whether this permission check is strict (deny by default)
     * or lenient (allow if no explicit deny)
     */
    boolean strict() default true;
}
