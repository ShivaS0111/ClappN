package biz.craftline.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * Fails fast when production-grade secrets / CORS are misconfigured.
 */
@Component
@Slf4j
public class ProductionStartupValidator implements ApplicationRunner {

    private static final String WEAK_DEFAULT =
            "myVerySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong";

    private final Environment environment;

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.require-strong-secret:false}")
    private boolean requireStrongSecret;

    @Value("${app.cors.allowed-origins:}")
    private String corsOrigins;

    public ProductionStartupValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean prod = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod"));

        if (requireStrongSecret || prod) {
            if (!StringUtils.hasText(jwtSecret) || jwtSecret.equals(WEAK_DEFAULT) || jwtSecret.length() < 32) {
                throw new IllegalStateException(
                        "JWT secret is missing, too short (<32 chars), or still the default. "
                                + "Set JWT_SECRET to a strong value before starting.");
            }
            if (!StringUtils.hasText(corsOrigins) || corsOrigins.contains("*")) {
                throw new IllegalStateException(
                        "APP_CORS_ORIGINS must be set to explicit frontend origin(s) when strong secrets are required.");
            }
            log.info("Production secret checks passed (JWT + CORS)");
        } else if (WEAK_DEFAULT.equals(jwtSecret)) {
            log.warn("Using default JWT secret — acceptable for local/dev only");
        }
    }
}
