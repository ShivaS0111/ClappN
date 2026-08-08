package biz.craftline.server.feature.paymentmanagement.application.service;

import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.PaymentProviderFactory;
import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import biz.craftline.server.feature.paymentmanagement.domain.service.PaymentService;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentInfoEntity;
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
    private final OrderRepository orderRepository;

    @Override
    public InitiatePaymentResponse initiate(InitiatePaymentRequest req) throws Exception {
        log.debug("Initiating payment for orderId: {}, gateway: {}", req.getOrderId(), req.getGateway());

        PaymentProvider provider = factory.providerFor(req.getGateway());
        if (provider == null) {
            String error = "Unsupported gateway: " + req.getGateway();
            log.error(error);
            throw new RuntimeException(error);
        }

        InitiatePaymentResponse resp = provider.initiatePayment(req);
        log.debug("Payment response from provider: paymentId={}, providerOrderId={}",
            resp.getPaymentId(), resp.getProviderOrderId());

        PaymentTransaction tx = PaymentTransaction.builder()
                .orderId(String.valueOf(req.getOrderId()))
                .gateway(resp.getGateway())
                .providerOrderId(resp.getProviderOrderId())
                .providerPaymentId(resp.getPaymentId())
                .currency(req.getCurrency())
                .amount(req.getAmount())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        txRepo.save(tx);
        log.info("Payment transaction saved: id={}, status={}", tx.getId(), tx.getStatus());

        return resp;
    }

    @Override
    @Transactional
    public PaymentTransaction confirm(String providerPaymentId) {
        PaymentTransaction tx = txRepo.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + providerPaymentId));

        if ("REFUNDED".equalsIgnoreCase(tx.getStatus()) || "FAILED".equalsIgnoreCase(tx.getStatus())) {
            throw new IllegalStateException("Cannot confirm payment in status: " + tx.getStatus());
        }
        if ("COMPLETED".equalsIgnoreCase(tx.getStatus()) || "SUCCESS".equalsIgnoreCase(tx.getStatus())) {
            return tx;
        }

        // Local confirm — gateway capture left to webhooks when real keys are configured
        tx.setStatus("COMPLETED");
        tx.setUpdatedAt(LocalDateTime.now());
        tx.setCompletedAt(LocalDateTime.now());
        PaymentTransaction saved = txRepo.save(tx);
        syncOrderPaymentInfo(saved, "PAID");
        log.info("Payment confirmed: providerPaymentId={}", providerPaymentId);
        return saved;
    }

    @Override
    @Transactional
    public PaymentTransaction refund(String providerPaymentId, Long amountMinorUnits) {
        PaymentTransaction tx = txRepo.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + providerPaymentId));

        if (!"COMPLETED".equalsIgnoreCase(tx.getStatus()) && !"SUCCESS".equalsIgnoreCase(tx.getStatus())
                && !"PARTIALLY_REFUNDED".equalsIgnoreCase(tx.getStatus())) {
            throw new IllegalStateException("Only completed payments can be refunded; status=" + tx.getStatus());
        }

        long refundAmount = amountMinorUnits != null ? amountMinorUnits : (tx.getAmount() != null ? tx.getAmount() : 0L);
        if (refundAmount <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        if (tx.getAmount() != null && refundAmount > tx.getAmount()) {
            throw new IllegalArgumentException("Refund amount exceeds original payment");
        }

        boolean full = tx.getAmount() == null || refundAmount >= tx.getAmount();
        tx.setStatus(full ? "REFUNDED" : "PARTIALLY_REFUNDED");
        tx.setUpdatedAt(LocalDateTime.now());
        PaymentTransaction saved = txRepo.save(tx);
        syncOrderPaymentInfo(saved, full ? "REFUNDED" : "PARTIALLY_REFUNDED");
        log.info("Payment refund recorded: providerPaymentId={} amount={} full={}",
                providerPaymentId, refundAmount, full);
        return saved;
    }

    private void syncOrderPaymentInfo(PaymentTransaction tx, String paymentStatus) {
        if (tx.getOrderId() == null || tx.getOrderId().isBlank()) {
            return;
        }
        try {
            Long orderId = Long.parseLong(tx.getOrderId().trim());
            orderRepository.findById(orderId).ifPresent(order -> {
                PaymentInfoEntity info = order.getPaymentInfo();
                if (info == null) {
                    info = new PaymentInfoEntity();
                    order.setPaymentInfo(info);
                }
                info.setPaymentMethod(tx.getGateway());
                if (tx.getAmount() != null) {
                    info.setAmount(tx.getAmount() / 100.0);
                }
                info.setStatus(paymentStatus);
                orderRepository.save(order);
            });
        } catch (NumberFormatException ignored) {
            log.warn("Invalid orderId on payment tx: {}", tx.getOrderId());
        }
    }
}
