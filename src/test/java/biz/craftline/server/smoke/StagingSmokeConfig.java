package biz.craftline.server.smoke;

/**
 * Staging smoke configuration via environment variables.
 *
 * <pre>
 * STAGING_BASE_URL   — default https://staging-api.craftlane.com
 * STAGING_EMAIL      — required
 * STAGING_PASSWORD   — required
 * STAGING_STORE_ID   — optional; resolved from /api/me/context when absent
 * STAGING_BUSINESS_ID — optional; resolved from context when absent
 * STAGING_SKIP_ORDER — true to skip order placement
 * </pre>
 */
public final class StagingSmokeConfig {

    private StagingSmokeConfig() {}

    public static final String BASE_URL = env("STAGING_BASE_URL", "https://staging-api.craftlane.com");
    public static final String EMAIL = env("STAGING_EMAIL", "");
    public static final String PASSWORD = env("STAGING_PASSWORD", "");
    public static final String STORE_ID = env("STAGING_STORE_ID", "");
    public static final String BUSINESS_ID = env("STAGING_BUSINESS_ID", "");
    public static final boolean SKIP_ORDER = Boolean.parseBoolean(env("STAGING_SKIP_ORDER", "false"));

    public static boolean isConfigured() {
        return !EMAIL.isBlank() && !PASSWORD.isBlank();
    }

    private static String env(String key, String defaultValue) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        return v.trim();
    }
}
