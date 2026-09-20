package biz.craftline.server.feature.paymentmanagement.application.provider;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("stripeProvider")
public class StripePaymentProvider implements PaymentProvider {

    @Value("${payment.stripe.secret}")
    private String stripeSecret;

    @Value("${payment.stripe.publishable:}")
    private String stripePublishable;

    @Value("${payment.stripe.webhook_secret:}")
    private String webhookSecret;

    @Value("${payment.stripe.success-url:http://localhost:5173/payment/callback}")
    private String defaultSuccessUrl;

    @Value("${payment.stripe.cancel-url:http://localhost:5173/payment/callback}")
    private String defaultCancelUrl;

    @Value("${app.payments.require-webhook-verification:false}")
    private boolean requireWebhookVerification;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecret;
        log.info("Stripe provider ready (publishable configured={})", StringUtils.hasText(stripePublishable));
    }

    @Override
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) throws Exception {
        try {
            long amountMinor = request.getAmount() != null ? request.getAmount() : 0L;
            if (amountMinor <= 0) {
                throw new IllegalArgumentException("Amount must be positive minor units");
            }

            String baseCallback = StringUtils.hasText(request.getCallbackUrl())
                    ? request.getCallbackUrl().trim()
                    : defaultSuccessUrl;
            String successUrl = appendQuery(baseCallback,
                    "orderId=" + request.getOrderId() + "&status=success&sessionId={CHECKOUT_SESSION_ID}");
            String cancelUrl = appendQuery(
                    StringUtils.hasText(request.getCallbackUrl()) ? request.getCallbackUrl().trim() : defaultCancelUrl,
                    "orderId=" + request.getOrderId() + "&status=cancelled");

            Map<String, String> metadata = new HashMap<>();
            metadata.put("orderId", String.valueOf(request.getOrderId()));
            metadata.put("gateway", "STRIPE");

            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putAllMetadata(metadata)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency().toLowerCase())
                                                    // request.amount is already minor units
                                                    .setUnitAmount(amountMinor)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + request.getOrderId())
                                                                    .build())
                                                    .build())
                                    .build())
                    .build();

            Session session = Session.create(params);
            log.info("Stripe Checkout Session created: sessionId={} orderId={} amountMinor={}",
                    session.getId(), request.getOrderId(), amountMinor);

            return InitiatePaymentResponse.builder()
                    .paymentId(session.getId())
                    .providerOrderId(session.getId())
                    .redirectUrl(session.getUrl())
                    .gateway("STRIPE")
                    .clientKey(stripePublishable)
                    .build();
        } catch (StripeException e) {
            log.error("Stripe API error: code={}, message={}", e.getCode(), e.getMessage());
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhook(String payload, String signatureHeader, Map<String, String> headers) {
        if (isPlaceholderSecret(webhookSecret)) {
            if (requireWebhookVerification) {
                log.error("Stripe webhook rejected: webhook secret missing or placeholder");
                return false;
            }
            log.warn("Stripe webhook verification skipped (dev): webhook secret not configured");
            return true;
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("Stripe webhook rejected: missing Stripe-Signature header");
            return false;
        }
        try {
            Webhook.constructEvent(payload, signatureHeader, webhookSecret);
            return true;
        } catch (com.stripe.exception.SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Stripe webhook verification error", e);
            return false;
        }
    }

    @Override
    public void refund(String providerPaymentId, String providerOrderId, Long amountMinorUnits, String currency)
            throws Exception {
        String paymentIntentId = resolvePaymentIntentId(providerPaymentId, providerOrderId);
        if (!StringUtils.hasText(paymentIntentId)) {
            throw new IllegalStateException(
                    "Cannot refund Stripe payment: no PaymentIntent for id=" + providerPaymentId
                            + " orderId=" + providerOrderId);
        }

        RefundCreateParams.Builder builder = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId);
        if (amountMinorUnits != null && amountMinorUnits > 0) {
            builder.setAmount(amountMinorUnits);
        }
        Refund refund = Refund.create(builder.build());
        log.info("Stripe refund created: refundId={} paymentIntent={} amount={}",
                refund.getId(), paymentIntentId, refund.getAmount());
    }

    private String resolvePaymentIntentId(String providerPaymentId, String providerOrderId) throws StripeException {
        if (StringUtils.hasText(providerPaymentId) && providerPaymentId.startsWith("pi_")) {
            return providerPaymentId;
        }
        String sessionId = null;
        if (StringUtils.hasText(providerPaymentId) && providerPaymentId.startsWith("cs_")) {
            sessionId = providerPaymentId;
        } else if (StringUtils.hasText(providerOrderId) && providerOrderId.startsWith("cs_")) {
            sessionId = providerOrderId;
        }
        if (sessionId != null) {
            Session session = Session.retrieve(sessionId);
            if (StringUtils.hasText(session.getPaymentIntent())) {
                return session.getPaymentIntent();
            }
        }
        if (StringUtils.hasText(providerPaymentId)) {
            try {
                PaymentIntent.retrieve(providerPaymentId);
                return providerPaymentId;
            } catch (StripeException ignored) {
                // fall through
            }
        }
        return null;
    }

    private static String appendQuery(String url, String query) {
        if (!StringUtils.hasText(url)) {
            return "?" + query;
        }
        return url.contains("?") ? url + "&" + query : url + "?" + query;
    }

    private static boolean isPlaceholderSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return true;
        }
        String s = secret.trim();
        return s.equals("whsec_TEST") || s.contains("YOUR_") || s.equalsIgnoreCase("changeme");
    }
}
