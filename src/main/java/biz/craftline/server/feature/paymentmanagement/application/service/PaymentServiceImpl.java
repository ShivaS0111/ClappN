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

import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentProviderFactory factory;
    private final PaymentTransactionRepository txRepo;

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

        // persist transaction
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
}
