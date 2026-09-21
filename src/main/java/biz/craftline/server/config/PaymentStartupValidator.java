package biz.craftline.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fails fast in prod when an enabled payment provider is missing real secrets.
 * Disable a provider via APP_PAYMENT_PROVIDERS (e.g. {@code stripe} only).
 */
@Component
@Order(20)
@Slf4j
public class PaymentStartupValidator implements ApplicationRunner {

    private final Environment environment;

    @Value("${app.payments.enabled-providers:stripe,razorpay}")
    private String enabledProviders;

    @Value("${app.payments.require-webhook-verification:false}")
    private boolean requireWebhookVerification;

    @Value("${payment.stripe.secret:}")
    private String stripeSecret;

    @Value("${payment.stripe.webhook_secret:}")
    private String stripeWebhookSecret;

    @Value("${payment.razorpay.key_secret:}")
    private String razorpayKeySecret;

    @Value("${payment.razorpay.webhook_secret:}")
    private String razorpayWebhookSecret;

    public PaymentStartupValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean prod = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod"));

        Set<String> providers = Arrays.stream(enabledProviders.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        if (!(prod || requireWebhookVerification)) {
            log.info("Payment startup checks relaxed (non-prod); enabledProviders={}", providers);
            return;
        }

        if (providers.contains("stripe")) {
            requireReal("STRIPE_SECRET / payment.stripe.secret", stripeSecret,
                    "sk_test_YOUR_STRIPE_SECRET", "sk_live_YOUR_");
            requireReal("STRIPE_WEBHOOK_SECRET / payment.stripe.webhook_secret", stripeWebhookSecret,
                    "whsec_TEST");
        }
        if (providers.contains("razorpay")) {
            requireReal("RAZORPAY_KEY_ID / payment.razorpay.key_id",
                    environment.getProperty("payment.razorpay.key_id", ""),
                    "rzp_test_YOUR_KEY_ID", "rzp_live_YOUR_");
            requireReal("RAZORPAY_KEY_SECRET / payment.razorpay.key_secret", razorpayKeySecret,
                    "YOUR_RAZORPAY_SECRET");
            requireReal("RAZORPAY_WEBHOOK_SECRET / payment.razorpay.webhook_secret", razorpayWebhookSecret,
                    "razorpay_test_secret");
        }

        log.info("Payment secret checks passed for providers={}", providers);
    }

    private static void requireReal(String name, String value, String... knownPlaceholders) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(
                    name + " is missing. Set the env var or remove the provider from APP_PAYMENT_PROVIDERS.");
        }
        String v = value.trim();
        if (v.contains("YOUR_") || v.equalsIgnoreCase("changeme")) {
            throw new IllegalStateException(
                    name + " still looks like a placeholder. Set a real secret before starting.");
        }
        for (String p : knownPlaceholders) {
            if (v.equals(p) || (p.endsWith("_") && v.startsWith(p))) {
                throw new IllegalStateException(
                        name + " is still the default/placeholder value. Set a real secret before starting.");
            }
        }
    }
}
