package biz.craftline.server.feature.paymentmanagement.application.service;

import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentInfoEntity;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Keeps order.paymentInfo in sync with payment_transaction status changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOrderSyncService {

    private final OrderRepository orderRepository;

    @Transactional
    public void syncFromTransaction(PaymentTransaction tx, String paymentStatus) {
        if (tx == null || tx.getOrderId() == null || tx.getOrderId().isBlank()) {
            return;
        }
        try {
            Long orderId = Long.parseLong(tx.getOrderId().trim());
            orderRepository.findById(orderId).ifPresent(order -> apply(order, tx, paymentStatus));
        } catch (NumberFormatException e) {
            log.warn("Invalid orderId on payment tx: {}", tx.getOrderId());
        }
    }

    private void apply(OrderEntity order, PaymentTransaction tx, String paymentStatus) {
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
        log.debug("Synced order {} paymentInfo status={}", order.getId(), paymentStatus);
    }
}
