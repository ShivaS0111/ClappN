package biz.craftline.server.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionStartupValidatorTest {

    @Mock private Environment environment;

    @Test
    void passesWhenNotProdAndWeakSecretAllowed() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        ProductionStartupValidator validator = new ProductionStartupValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret",
                "myVerySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong");
        ReflectionTestUtils.setField(validator, "requireStrongSecret", false);
        ReflectionTestUtils.setField(validator, "corsOrigins", "*");

        assertDoesNotThrow(() -> validator.run(new DefaultApplicationArguments()));
    }

    @Test
    void failsInProdWhenDefaultJwtSecret() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        ProductionStartupValidator validator = new ProductionStartupValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret",
                "myVerySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong");
        ReflectionTestUtils.setField(validator, "requireStrongSecret", false);
        ReflectionTestUtils.setField(validator, "corsOrigins", "https://app.example.com");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validator.run(new DefaultApplicationArguments()));
        assertTrue(ex.getMessage().contains("JWT secret"));
    }

    @Test
    void failsWhenCorsWildcardWithStrongSecret() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        ProductionStartupValidator validator = new ProductionStartupValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret",
                "a-strong-enough-secret-value-32chars-min");
        ReflectionTestUtils.setField(validator, "requireStrongSecret", true);
        ReflectionTestUtils.setField(validator, "corsOrigins", "*");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validator.run(new DefaultApplicationArguments()));
        assertTrue(ex.getMessage().contains("APP_CORS_ORIGINS"));
    }

    @Test
    void passesWithStrongSecretAndExplicitCors() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        ProductionStartupValidator validator = new ProductionStartupValidator(environment);
        ReflectionTestUtils.setField(validator, "jwtSecret",
                "a-strong-enough-secret-value-32chars-min");
        ReflectionTestUtils.setField(validator, "requireStrongSecret", true);
        ReflectionTestUtils.setField(validator, "corsOrigins", "https://app.example.com");

        assertDoesNotThrow(() -> validator.run(new DefaultApplicationArguments()));
    }
}
