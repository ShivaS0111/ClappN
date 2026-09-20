package biz.craftline.server.feature.paymentmanagement.domain.provider;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;

import java.util.Map;

/**
 * Gateway adapter. Amounts on {@link InitiatePaymentRequest} are always minor units (cents/paise).
 */
public interface PaymentProvider {
    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) throws Exception;

    boolean verifyWebhook(String payload, String signatureHeader, Map<String, String> headers) throws Exception;

    /**
     * Refund at the gateway. {@code providerPaymentId} / {@code providerOrderId} are whatever
     * was stored at initiate time (session id, payment intent, Razorpay payment id, etc.).
     *
     * @param amountMinorUnits null = full refund of original capture where supported
     */
    void refund(String providerPaymentId, String providerOrderId, Long amountMinorUnits, String currency)
            throws Exception;
}
