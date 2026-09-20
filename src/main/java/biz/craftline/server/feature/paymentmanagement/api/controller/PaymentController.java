package biz.craftline.server.feature.paymentmanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.service.PaymentService;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentTransactionRepository txRepo;
    private final OrderRepository orderRepository;
    private final SecurityContextService securityContextService;

    @PostMapping("/initiate")
    @RequirePermission("payment.create")
    public ResponseEntity<?> initiate(@RequestBody InitiatePaymentRequest req) {
        try {
            if (req.getOrderId() == null || req.getOrderId() <= 0) {
                return ResponseEntity.badRequest().body("Invalid orderId");
            }
        // Amounts are always minor units (cents/paise)
        if (req.getAmount() == null || req.getAmount() <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount (use minor units, e.g. paise/cents)");
        }
            if (req.getCurrency() == null || req.getCurrency().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Currency is required");
            }
            if (req.getGateway() == null || req.getGateway().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Payment gateway is required");
            }

            // Scope: caller must access the order's store
            OrderEntity order = orderRepository.findById(req.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + req.getOrderId()));
            securityContextService.validateStoreAccess(order.getStoreId());

            log.info("Initiating payment: orderId={}, amount={}, currency={}, gateway={}",
                    req.getOrderId(), req.getAmount(), req.getCurrency(), req.getGateway());

            InitiatePaymentResponse response = paymentService.initiate(req);
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Payment initiation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment initiation failed: " + e.getMessage());
        }
    }

    @GetMapping("/status/{providerPaymentId}")
    @RequirePermission("payment.read")
    public ResponseEntity<?> status(@PathVariable String providerPaymentId) {
        try {
            if (providerPaymentId == null || providerPaymentId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("providerPaymentId is required");
            }
            Optional<PaymentTransaction> tx = txRepo.findByProviderPaymentId(providerPaymentId);
            if (tx.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Payment transaction not found");
            }

            PaymentTransaction payment = tx.get();
            assertCanAccessPayment(payment);
            return ResponseEntity.ok(payment);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching status: " + e.getMessage());
        }
    }

    @PostMapping("/{providerPaymentId}/confirm")
    @RequirePermission("payment.create")
    public ResponseEntity<?> confirm(@PathVariable String providerPaymentId) {
        try {
            PaymentTransaction existing = txRepo.findByProviderPaymentId(providerPaymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));
            assertCanAccessPayment(existing);
            PaymentTransaction confirmed = paymentService.confirm(providerPaymentId);
            return ResponseEntity.ok(confirmed);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Payment confirm failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment confirm failed: " + e.getMessage());
        }
    }

    @PostMapping("/{providerPaymentId}/refund")
    @RequirePermission("payment.refund")
    public ResponseEntity<?> refund(
            @PathVariable String providerPaymentId,
            @RequestBody(required = false) biz.craftline.server.feature.paymentmanagement.api.request.RefundPaymentRequest body) {
        try {
            PaymentTransaction existing = txRepo.findByProviderPaymentId(providerPaymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));
            assertCanAccessPayment(existing);
            Long amount = body != null ? body.getAmount() : null;
            PaymentTransaction refunded = paymentService.refund(providerPaymentId, amount);
            return ResponseEntity.ok(refunded);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Payment refund failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment refund failed: " + e.getMessage());
        }
    }

    private void assertCanAccessPayment(PaymentTransaction payment) {
        if (securityContextService.isSystemAdmin()) {
            return;
        }
        String orderIdRaw = payment.getOrderId();
        if (orderIdRaw == null || orderIdRaw.isBlank()) {
            throw new AccessDeniedException("Payment is not linked to an accessible order");
        }
        try {
            Long orderId = Long.parseLong(orderIdRaw.trim());
            OrderEntity order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AccessDeniedException("Order not found for payment"));
            securityContextService.validateStoreAccess(order.getStoreId());
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid order reference on payment");
        }
    }
}
