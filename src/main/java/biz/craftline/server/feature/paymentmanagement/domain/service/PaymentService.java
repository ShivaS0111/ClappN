package biz.craftline.server.feature.paymentmanagement.domain.service;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;

public interface PaymentService {
    InitiatePaymentResponse initiate(InitiatePaymentRequest req) throws Exception;

    PaymentTransaction confirm(String providerPaymentId);

    PaymentTransaction refund(String providerPaymentId, Long amountMinorUnits);
}
