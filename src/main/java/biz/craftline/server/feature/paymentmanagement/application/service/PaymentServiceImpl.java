package biz.craftline.server.feature.paymentmanagement.application.service;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.PaymentProviderFactory;
import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import biz.craftline.server.feature.paymentmanagement.domain.service.PaymentService;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentProviderFactory factory;
    private final PaymentTransactionRepository txRepo;
    private final PaymentOrderSyncService orderSyncService;

    @Override
    public InitiatePaymentResponse initiate(InitiatePaymentRequest req) throws Exception {
        log.debug("Initiating payment for orderId: {}, gateway: {}", req.getOrderId(), req.getGateway());

        PaymentProvider provider = factory.providerFor(req.getGateway());
        if (provider == null) {
            throw new RuntimeException("Unsupported gateway: " + req.getGateway());
        }

        InitiatePaymentResponse resp = provider.initiatePayment(req);

        PaymentTransaction tx = PaymentTransaction.builder()
                .orderId(String.valueOf(req.getOrderId()))
                .gateway(resp.getGateway())
                .providerOrderId(resp.getProviderOrderId())
                .providerPaymentId(resp.getPaymentId())
                .currency(req.getCurrency())
                .amount(req.getAmount())
                .callbackUrl(req.getCallbackUrl())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        txRepo.save(tx);
        log.info("Payment transaction saved: id={}, gateway={}, providerPaymentId={}",
                tx.getId(), tx.getGateway(), tx.getProviderPaymentId());

        return resp;
    }

    @Override
    @Transactional
    public PaymentTransaction confirm(String providerPaymentId) {
        PaymentTransaction tx = findTx(providerPaymentId);

        if ("REFUNDED".equalsIgnoreCase(tx.getStatus()) || "FAILED".equalsIgnoreCase(tx.getStatus())) {
            throw new IllegalStateException("Cannot confirm payment in status: " + tx.getStatus());
        }
        if ("COMPLETED".equalsIgnoreCase(tx.getStatus()) || "SUCCESS".equalsIgnoreCase(tx.getStatus())) {
            return tx;
        }

        tx.setStatus("COMPLETED");
        tx.setUpdatedAt(LocalDateTime.now());
        tx.setCompletedAt(LocalDateTime.now());
        PaymentTransaction saved = txRepo.save(tx);
        orderSyncService.syncFromTransaction(saved, "PAID");
        log.info("Payment confirmed locally: providerPaymentId={}", providerPaymentId);
        return saved;
    }

    @Override
    @Transactional
    public PaymentTransaction refund(String providerPaymentId, Long amountMinorUnits) {
        PaymentTransaction tx = findTx(providerPaymentId);

        if (!"COMPLETED".equalsIgnoreCase(tx.getStatus()) && !"SUCCESS".equalsIgnoreCase(tx.getStatus())
                && !"PARTIALLY_REFUNDED".equalsIgnoreCase(tx.getStatus())) {
            throw new IllegalStateException("Only completed payments can be refunded; status=" + tx.getStatus());
        }

        long refundAmount = amountMinorUnits != null ? amountMinorUnits
                : (tx.getAmount() != null ? tx.getAmount() : 0L);
        if (refundAmount <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        if (tx.getAmount() != null && refundAmount > tx.getAmount()) {
            throw new IllegalArgumentException("Refund amount exceeds original payment");
        }

        PaymentProvider provider = factory.providerFor(tx.getGateway());
        if (provider == null) {
            throw new IllegalStateException("No provider for gateway: " + tx.getGateway());
        }
        try {
            provider.refund(tx.getProviderPaymentId(), tx.getProviderOrderId(), amountMinorUnits, tx.getCurrency());
        } catch (Exception e) {
            log.error("Gateway refund failed for {}", providerPaymentId, e);
            throw new RuntimeException("Gateway refund failed: " + e.getMessage(), e);
        }

        boolean full = tx.getAmount() == null || refundAmount >= tx.getAmount();
        tx.setStatus(full ? "REFUNDED" : "PARTIALLY_REFUNDED");
        tx.setUpdatedAt(LocalDateTime.now());
        PaymentTransaction saved = txRepo.save(tx);
        orderSyncService.syncFromTransaction(saved, full ? "REFUNDED" : "PARTIALLY_REFUNDED");
        log.info("Payment refund recorded: providerPaymentId={} amount={} full={}",
                providerPaymentId, refundAmount, full);
        return saved;
    }

    private PaymentTransaction findTx(String providerPaymentId) {
        return txRepo.findByProviderPaymentId(providerPaymentId)
                .or(() -> txRepo.findByProviderOrderId(providerPaymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + providerPaymentId));
    }
}
