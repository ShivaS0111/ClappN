package biz.craftline.server.feature.paymentmanagement.application.provider;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

/**
 * Razorpay Orders API initiate + HMAC webhook verify + payment refunds.
 */
@Slf4j
@Service("razorpayProvider")
public class RazorpayPaymentProvider implements PaymentProvider {

    private static final String API_BASE = "https://api.razorpay.com/v1";

    @Value("${payment.razorpay.key_id}")
    private String keyId;

    @Value("${payment.razorpay.key_secret}")
    private String keySecret;

    @Value("${payment.razorpay.webhook_secret:}")
    private String webhookSecret;

    @Value("${app.payments.require-webhook-verification:false}")
    private boolean requireWebhookVerification;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Override
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) throws Exception {
        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(keySecret)
                || keyId.contains("YOUR_") || keySecret.contains("YOUR_")) {
            throw new IllegalStateException(
                    "Razorpay keys not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
        }
        long amountMinor = request.getAmount() != null ? request.getAmount() : 0L;
        if (amountMinor <= 0) {
            throw new IllegalArgumentException("Amount must be positive minor units");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("amount", amountMinor);
        body.put("currency", request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR");
        body.put("receipt", "order_" + request.getOrderId());
        ObjectNode notes = body.putObject("notes");
        notes.put("orderId", String.valueOf(request.getOrderId()));
        notes.put("gateway", "RAZORPAY");

        HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + "/orders"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());
        if (httpResp.statusCode() < 200 || httpResp.statusCode() >= 300) {
            log.error("Razorpay order create failed: status={} body={}", httpResp.statusCode(), httpResp.body());
            throw new RuntimeException("Razorpay order create failed: HTTP " + httpResp.statusCode());
        }

        JsonNode created = objectMapper.readTree(httpResp.body());
        String razorpayOrderId = created.path("id").asText();
        if (!StringUtils.hasText(razorpayOrderId)) {
            throw new RuntimeException("Razorpay response missing order id");
        }

        log.info("Razorpay order created: razorpayOrderId={} clappOrderId={} amount={}",
                razorpayOrderId, request.getOrderId(), amountMinor);

        // Client opens Checkout with order_id + key_id; paymentId filled later from webhook
        return InitiatePaymentResponse.builder()
                .paymentId(razorpayOrderId)
                .providerOrderId(razorpayOrderId)
                .redirectUrl(null)
                .gateway("RAZORPAY")
                .clientKey(keyId)
                .build();
    }

    @Override
    public boolean verifyWebhook(String payload, String signatureHeader, Map<String, String> headers) {
        if (isPlaceholderSecret(webhookSecret)) {
            if (requireWebhookVerification) {
                log.error("Razorpay webhook rejected: webhook secret missing or placeholder");
                return false;
            }
            log.warn("Razorpay webhook verification skipped (dev): webhook secret not configured");
            return true;
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("Razorpay webhook rejected: missing X-Razorpay-Signature header");
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.trim().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Razorpay webhook verification error", e);
            return false;
        }
    }

    @Override
    public void refund(String providerPaymentId, String providerOrderId, Long amountMinorUnits, String currency)
            throws Exception {
        String paymentId = providerPaymentId;
        if (!StringUtils.hasText(paymentId) || paymentId.startsWith("order_")) {
            throw new IllegalStateException(
                    "Razorpay refund requires a payment id (pay_...), got paymentId=" + providerPaymentId
                            + " orderId=" + providerOrderId
                            + ". Wait for payment.captured webhook before refunding.");
        }

        ObjectNode body = objectMapper.createObjectNode();
        if (amountMinorUnits != null && amountMinorUnits > 0) {
            body.put("amount", amountMinorUnits);
        }

        HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + "/payments/" + paymentId + "/refund"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());
        if (httpResp.statusCode() < 200 || httpResp.statusCode() >= 300) {
            log.error("Razorpay refund failed: status={} body={}", httpResp.statusCode(), httpResp.body());
            throw new RuntimeException("Razorpay refund failed: HTTP " + httpResp.statusCode() + " " + httpResp.body());
        }
        log.info("Razorpay refund ok: paymentId={} response={}", paymentId, httpResp.body());
    }

    private String basicAuth() {
        String raw = keyId + ":" + keySecret;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isPlaceholderSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return true;
        }
        String s = secret.trim();
        return s.equals("razorpay_test_secret")
                || s.equals("YOUR_RAZORPAY_SECRET")
                || s.contains("YOUR_")
                || s.equalsIgnoreCase("changeme");
    }
}
