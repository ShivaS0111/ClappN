package biz.craftline.server.feature.paymentmanagement.api.controller;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.service.PaymentService;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(@RequestBody InitiatePaymentRequest req) {
        try {
            // Validation
            if (req.getOrderId() == null || req.getOrderId() <= 0) {
                return ResponseEntity.badRequest().body("Invalid orderId");
            }
            if (req.getAmount() == null || req.getAmount() <= 0) {
                return ResponseEntity.badRequest().body("Invalid amount");
            }
            if (req.getCurrency() == null || req.getCurrency().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Currency is required");
            }
            if (req.getGateway() == null || req.getGateway().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Payment gateway is required");
            }

            log.info("Initiating payment: orderId={}, amount={}, currency={}, gateway={}",
                req.getOrderId(), req.getAmount(), req.getCurrency(), req.getGateway());

            InitiatePaymentResponse response = paymentService.initiate(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Payment initiation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment initiation failed: " + e.getMessage());
        }
    }

    @GetMapping("/status/{providerPaymentId}")
    public ResponseEntity<?> status(@PathVariable String providerPaymentId) {
        try {
            if (providerPaymentId == null || providerPaymentId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("providerPaymentId is required");
            }
            Optional<PaymentTransaction> tx = txRepo.findByProviderPaymentId(providerPaymentId);
            if (tx.isPresent()) {
                return ResponseEntity.ok(tx.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Payment transaction not found");
            }
        } catch (Exception e) {
            log.error("Error fetching payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching status: " + e.getMessage());
        }
    }
}
