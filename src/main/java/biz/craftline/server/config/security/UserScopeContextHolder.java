package biz.craftline.server.config.security;

/**
 * Thread-local holder for the current request's {@link UserScopeContext}.
 * Must be cleared at the end of each request to avoid leaks on pooled threads.
 */
public final class UserScopeContextHolder {

    private static final ThreadLocal<UserScopeContext> CONTEXT = new ThreadLocal<>();

    private UserScopeContextHolder() {
    }

    public static void set(UserScopeContext context) {
        CONTEXT.set(context);
    }

    public static UserScopeContext get() {
        return CONTEXT.get();
    }

    public static UserScopeContext require() {
        UserScopeContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new IllegalStateException("UserScopeContext is not available for this request");
        }
        return ctx;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static boolean isPresent() {
        return CONTEXT.get() != null;
    }
}
