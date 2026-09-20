package biz.craftline.server.feature.paymentmanagement.api.controller;

import biz.craftline.server.feature.paymentmanagement.application.service.PaymentOrderSyncService;
import biz.craftline.server.feature.paymentmanagement.domain.PaymentProviderFactory;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class WebhookController {

    private static final Set<String> TERMINAL = Set.of("COMPLETED", "REFUNDED", "FAILED");

    private final PaymentProviderFactory factory;
    private final PaymentTransactionRepository txRepo;
    private final ObjectMapper objectMapper;
    private final PaymentOrderSyncService orderSyncService;

    @PostMapping("/stripe")
    public ResponseEntity<?> stripeWebhook(@RequestBody String payload, @RequestHeader Map<String, String> headers) {
        String signature = headerIgnoreCase(headers, "stripe-signature");
        try {
            log.info("Stripe webhook received");
            var provider = factory.providerFor("STRIPE");
            if (provider == null || !provider.verifyWebhook(payload, signature, headers)) {
                log.warn("Stripe webhook rejected: signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Signature verification failed"));
            }

            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.path("type").asText();
            JsonNode data = event.path("data").path("object");
            handleStripeEvent(eventType, data);

            log.info("Stripe webhook accepted: eventType={}", eventType);
            return ResponseEntity.ok(Map.of("received", true, "verified", true));
        } catch (Exception e) {
            log.error("Stripe webhook error", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/razorpay")
    public ResponseEntity<?> razorpayWebhook(@RequestBody String payload, @RequestHeader Map<String, String> headers) {
        String signature = headerIgnoreCase(headers, "x-razorpay-signature");
        try {
            log.info("Razorpay webhook received");
            var provider = factory.providerFor("RAZORPAY");
            if (provider == null || !provider.verifyWebhook(payload, signature, headers)) {
                log.warn("Razorpay webhook rejected: signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Signature verification failed"));
            }

            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.path("event").asText();
            handleRazorpayEvent(eventType, event);

            log.info("Razorpay webhook accepted: eventType={}", eventType);
            return ResponseEntity.ok(Map.of("received", true, "verified", true));
        } catch (Exception e) {
            log.error("Razorpay webhook error", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private static String headerIgnoreCase(Map<String, String> headers, String name) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
                return e.getValue() != null ? e.getValue() : "";
            }
        }
        return "";
    }

    private void handleStripeEvent(String eventType, JsonNode data) {
        if (data == null || data.isMissingNode()) {
            log.warn("Stripe event missing data.object: {}", eventType);
            return;
        }

        Optional<PaymentTransaction> txOpt = Optional.empty();
        String mappedStatus = null;

        if ("checkout.session.completed".equals(eventType)
                || "checkout.session.async_payment_succeeded".equals(eventType)) {
            String sessionId = text(data, "id");
            String paymentIntent = text(data, "payment_intent");
            txOpt = findTx(sessionId, sessionId);
            mappedStatus = "COMPLETED";
            txOpt.ifPresent(tx -> {
                if (StringUtils.hasText(paymentIntent)) {
                    tx.setProviderPaymentId(paymentIntent);
                }
                if (!StringUtils.hasText(tx.getProviderOrderId())) {
                    tx.setProviderOrderId(sessionId);
                }
            });
        } else if ("checkout.session.expired".equals(eventType)
                || "checkout.session.async_payment_failed".equals(eventType)) {
            String sessionId = text(data, "id");
            txOpt = findTx(sessionId, sessionId);
            mappedStatus = "FAILED";
        } else if ("payment_intent.succeeded".equals(eventType)) {
            String pi = text(data, "id");
            txOpt = findTx(pi, null);
            mappedStatus = "COMPLETED";
        } else if ("payment_intent.payment_failed".equals(eventType)) {
            String pi = text(data, "id");
            txOpt = findTx(pi, null);
            mappedStatus = "FAILED";
        } else if ("charge.refunded".equals(eventType) || "charge.refund.updated".equals(eventType)) {
            String pi = text(data, "payment_intent");
            txOpt = findTx(pi, null);
            boolean full = data.path("refunded").asBoolean(false)
                    || data.path("amount_refunded").asLong(0) >= data.path("amount").asLong(Long.MAX_VALUE);
            mappedStatus = full ? "REFUNDED" : "PARTIALLY_REFUNDED";
        } else {
            log.info("Stripe event ignored: {}", eventType);
            return;
        }

        if (txOpt.isEmpty()) {
            log.warn("Stripe event for unknown payment: type={} id={}", eventType, text(data, "id"));
            return;
        }
        applyStatus(txOpt.get(), mappedStatus);
    }

    private void handleRazorpayEvent(String eventType, JsonNode root) {
        JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
        if (paymentEntity.isMissingNode() || paymentEntity.isEmpty()) {
            log.info("Razorpay event without payment.entity ignored: {}", eventType);
            return;
        }

        String paymentId = text(paymentEntity, "id");
        String orderId = text(paymentEntity, "order_id");
        String status = text(paymentEntity, "status");

        Optional<PaymentTransaction> txOpt = findTx(paymentId, orderId);
        if (txOpt.isEmpty()) {
            log.warn("Razorpay event for unknown payment: paymentId={} orderId={}", paymentId, orderId);
            return;
        }

        PaymentTransaction tx = txOpt.get();
        if (StringUtils.hasText(paymentId)) {
            tx.setProviderPaymentId(paymentId);
        }
        if (StringUtils.hasText(orderId) && !StringUtils.hasText(tx.getProviderOrderId())) {
            tx.setProviderOrderId(orderId);
        }

        String mapped;
        if (eventType != null && eventType.contains("refund")) {
            mapped = "REFUNDED";
        } else {
            mapped = mapRazorpayStatus(status);
        }
        applyStatus(tx, mapped);
    }

    private Optional<PaymentTransaction> findTx(String paymentOrSessionId, String orderOrSessionId) {
        if (StringUtils.hasText(paymentOrSessionId)) {
            Optional<PaymentTransaction> byPay = txRepo.findByProviderPaymentId(paymentOrSessionId);
            if (byPay.isPresent()) {
                return byPay;
            }
            Optional<PaymentTransaction> byOrd = txRepo.findByProviderOrderId(paymentOrSessionId);
            if (byOrd.isPresent()) {
                return byOrd;
            }
        }
        if (StringUtils.hasText(orderOrSessionId)) {
            Optional<PaymentTransaction> byOrd = txRepo.findByProviderOrderId(orderOrSessionId);
            if (byOrd.isPresent()) {
                return byOrd;
            }
            return txRepo.findByProviderPaymentId(orderOrSessionId);
        }
        return Optional.empty();
    }

    private void applyStatus(PaymentTransaction transaction, String txStatus) {
        if (txStatus == null) {
            return;
        }
        String current = transaction.getStatus() != null ? transaction.getStatus() : "";
        if (TERMINAL.contains(current.toUpperCase()) && current.equalsIgnoreCase(txStatus)) {
            log.debug("Idempotent webhook: tx {} already {}", transaction.getId(), current);
            return;
        }
        if ("REFUNDED".equalsIgnoreCase(current) && !"REFUNDED".equalsIgnoreCase(txStatus)) {
            log.debug("Ignoring status {} for already refunded tx {}", txStatus, transaction.getId());
            return;
        }

        transaction.setStatus(txStatus);
        transaction.setUpdatedAt(LocalDateTime.now());
        if ("COMPLETED".equals(txStatus)) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        txRepo.save(transaction);

        String orderPaymentStatus = switch (txStatus) {
            case "COMPLETED" -> "PAID";
            case "REFUNDED" -> "REFUNDED";
            case "PARTIALLY_REFUNDED" -> "PARTIALLY_REFUNDED";
            case "FAILED" -> "FAILED";
            default -> txStatus;
        };
        orderSyncService.syncFromTransaction(transaction, orderPaymentStatus);
        log.info("Updated transaction {} to status {} via webhook", transaction.getId(), txStatus);
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) {
            return "";
        }
        return v.asText("");
    }

    private String mapRazorpayStatus(String razorpayStatus) {
        if (razorpayStatus == null) {
            return "PENDING";
        }
        return switch (razorpayStatus) {
            case "captured" -> "COMPLETED";
            case "authorized" -> "PROCESSING";
            case "created" -> "PENDING";
            case "refunded" -> "REFUNDED";
            case "failed" -> "FAILED";
            default -> "FAILED";
        };
    }
}
