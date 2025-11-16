package biz.craftline.server.feature.paymentmanagement.domain.service;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;

public interface PaymentService {
    InitiatePaymentResponse initiate(InitiatePaymentRequest req) throws Exception;
}
