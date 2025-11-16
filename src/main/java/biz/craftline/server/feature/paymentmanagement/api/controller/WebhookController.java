package biz.craftline.server.feature.paymentmanagement.api.controller;

import biz.craftline.server.feature.paymentmanagement.domain.PaymentProviderFactory;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class WebhookController {
    private final PaymentProviderFactory factory;
    private final PaymentTransactionRepository txRepo;
    private final ObjectMapper objectMapper;

    @PostMapping("/stripe")
    public ResponseEntity<?> stripeWebhook(@RequestBody String payload, @RequestHeader Map<String, String> headers) {
        try {
            log.debug("Received Stripe webhook");
            var provider = factory.providerFor("STRIPE");
            boolean verified = provider.verifyWebhook(payload, headers.getOrDefault("stripe-signature", ""), headers);

            if (!verified) {
                log.warn("Stripe webhook signature verification failed");
                return ResponseEntity.badRequest().body(Map.of("error", "Signature verification failed"));
            }

            // Parse Stripe event
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.get("type").asText();
            JsonNode data = event.get("data").get("object");

            handleStripeEvent(eventType, data);

            return ResponseEntity.ok(Map.of("received", true, "verified", true));
        } catch (Exception e) {
            log.error("Stripe webhook error", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/razorpay")
    public ResponseEntity<?> razorpayWebhook(@RequestBody String payload, @RequestHeader Map<String, String> headers) {
        try {
            log.debug("Received Razorpay webhook");
            var provider = factory.providerFor("RAZORPAY");
            boolean verified = provider.verifyWebhook(payload, headers.getOrDefault("x-razorpay-signature", ""), headers);

            if (!verified) {
                log.warn("Razorpay webhook signature verification failed");
                return ResponseEntity.badRequest().body(Map.of("error", "Signature verification failed"));
            }

            // Parse Razorpay event
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.get("event").asText();
            JsonNode data = event.get("payload").get("payment").get("entity");

            handleRazorpayEvent(eventType, data);

            return ResponseEntity.ok(Map.of("received", true, "verified", true));
        } catch (Exception e) {
            log.error("Razorpay webhook error", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void handleStripeEvent(String eventType, JsonNode data) {
        String stripePaymentId = data.get("id").asText();
        String status = data.get("status").asText();

        Optional<PaymentTransaction> tx = txRepo.findByProviderPaymentId(stripePaymentId);
        if (tx.isEmpty()) {
            log.warn("Stripe event for unknown payment: {}", stripePaymentId);
            return;
        }

        PaymentTransaction transaction = tx.get();
        String txStatus = mapStripeStatus(status);

        transaction.setStatus(txStatus);
        transaction.setUpdatedAt(LocalDateTime.now());
        if ("COMPLETED".equals(txStatus)) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        txRepo.save(transaction);

        log.info("Updated transaction {} to status {} via Stripe webhook", transaction.getId(), txStatus);
    }

    private void handleRazorpayEvent(String eventType, JsonNode data) {
        String razorpayPaymentId = data.get("id").asText();
        String status = data.get("status").asText();

        Optional<PaymentTransaction> tx = txRepo.findByProviderPaymentId(razorpayPaymentId);
        if (tx.isEmpty()) {
            log.warn("Razorpay event for unknown payment: {}", razorpayPaymentId);
            return;
        }

        PaymentTransaction transaction = tx.get();
        String txStatus = mapRazorpayStatus(status);

        transaction.setStatus(txStatus);
        transaction.setUpdatedAt(LocalDateTime.now());
        if ("COMPLETED".equals(txStatus)) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        txRepo.save(transaction);

        log.info("Updated transaction {} to status {} via Razorpay webhook", transaction.getId(), txStatus);
    }

    private String mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "succeeded" -> "COMPLETED";
            case "processing" -> "PROCESSING";
            case "requires_payment_method", "requires_action" -> "PENDING";
            default -> "FAILED";
        };
    }

    private String mapRazorpayStatus(String razorpayStatus) {
        return switch (razorpayStatus) {
            case "captured" -> "COMPLETED";
            case "authorized" -> "PROCESSING";
            case "created" -> "PENDING";
            default -> "FAILED";
        };
    }
}