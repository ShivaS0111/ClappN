package biz.craftline.server.feature.paymentmanagement.domain.provider;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;

import java.util.Map;

public interface PaymentProvider {
    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) throws Exception;
    boolean verifyWebhook(String payload, String signatureHeader, Map<String,String> headers) throws Exception;
}