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
class PaymentStartupValidatorTest {

    @Mock private Environment environment;

    private PaymentStartupValidator validatorWith(
            String providers,
            boolean requireWebhook,
            String stripeSecret,
            String stripeWebhook,
            String razorSecret,
            String razorWebhook) {
        PaymentStartupValidator v = new PaymentStartupValidator(environment);
        ReflectionTestUtils.setField(v, "enabledProviders", providers);
        ReflectionTestUtils.setField(v, "requireWebhookVerification", requireWebhook);
        ReflectionTestUtils.setField(v, "stripeSecret", stripeSecret);
        ReflectionTestUtils.setField(v, "stripeWebhookSecret", stripeWebhook);
        ReflectionTestUtils.setField(v, "razorpayKeySecret", razorSecret);
        ReflectionTestUtils.setField(v, "razorpayWebhookSecret", razorWebhook);
        return v;
    }

    @Test
    void relaxedOutsideProd() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        PaymentStartupValidator v = validatorWith("stripe,razorpay", false,
                "", "", "", "");
        assertDoesNotThrow(() -> v.run(new DefaultApplicationArguments()));
    }

    @Test
    void failsInProdWhenStripePlaceholder() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        PaymentStartupValidator v = validatorWith("stripe", false,
                "sk_test_YOUR_STRIPE_SECRET", "whsec_real", "", "");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> v.run(new DefaultApplicationArguments()));
        assertTrue(ex.getMessage().contains("STRIPE_SECRET"));
    }

    @Test
    void failsWhenMissingSecret() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(environment.getProperty("payment.razorpay.key_id", "")).thenReturn("rzp_test_real");
        PaymentStartupValidator v = validatorWith("razorpay", true,
                "", "", "", "secret");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> v.run(new DefaultApplicationArguments()));
        assertTrue(ex.getMessage().contains("RAZORPAY_KEY_SECRET"));
    }

    @Test
    void passesWithRealSecrets() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("payment.razorpay.key_id", "")).thenReturn("rzp_live_real_key");
        PaymentStartupValidator v = validatorWith("stripe,razorpay", true,
                "sk_live_real_secret_value",
                "whsec_real_webhook",
                "rzp_real_secret",
                "rzp_wh_real");
        assertDoesNotThrow(() -> v.run(new DefaultApplicationArguments()));
    }
}
