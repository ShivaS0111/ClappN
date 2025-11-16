package biz.craftline.server.feature.paymentmanagement.application.provider;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * STUBBED provider. For real integration:
 * - add razorpay SDK
 * - create Order via Razorpay Orders API (amount in paise)
 * - return necessary keys/order id for frontend checkout
 */
@Service("razorpayProvider")
public class RazorpayPaymentProvider implements PaymentProvider {
    @Value("${payment.razorpay.key_id}") private String keyId;
    @Value("${payment.razorpay.key_secret}") private String keySecret;

    @Override
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) throws Exception {
        String providerOrderId = "order_RP_" + System.currentTimeMillis();
        String token = String.format("{\"orderId\":%d,\"amount\":%d,\"currency\":\"%s\",\"gateway\":\"RAZORPAY\"}",
            request.getOrderId(), request.getAmount(), request.getCurrency());
        String redirectUrl = "/pay/index.html#token=" + java.util.Base64.getUrlEncoder().encodeToString(token.getBytes());
        return InitiatePaymentResponse.builder()
                .paymentId("PAY_RP_" + System.currentTimeMillis())
                .providerOrderId(providerOrderId)
                .redirectUrl(redirectUrl)
                .gateway("RAZORPAY")
                .build();
    }

    @Override
    public boolean verifyWebhook(String payload, String signatureHeader, Map<String, String> headers) throws Exception {
        // Verify signature using your Razorpay webhook secret
        return true;
    }
}
