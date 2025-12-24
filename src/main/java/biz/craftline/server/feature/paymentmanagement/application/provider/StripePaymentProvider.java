package biz.craftline.server.feature.paymentmanagement.application.provider;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service("stripeProvider")
public class StripePaymentProvider implements PaymentProvider {
    @Value("${payment.stripe.secret}")
    private String stripeSecret;

    @Value("${payment.stripe.publishable}")
    private String stripePublishable;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecret;
        log.info("Stripe initialized with publishable key: {}", stripePublishable);
    }

    @Override
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) throws Exception {
        try {
            log.debug("Creating Stripe checkout session: orderId={}, amount={}", request.getOrderId(), request.getAmount());

            String callbackUrl = request.getCallbackUrl() != null ? request.getCallbackUrl() : "https://thenewzapp.com/payment/callback";
             callbackUrl ="https://thenewzapp.com/payment/callback";

            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    //.setReturnUrl("https://thenewzapp.com/payment/callback")
                    .setSuccessUrl(callbackUrl + "?orderId=" + request.getOrderId() + "&status=success&sessionId={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(callbackUrl + "?orderId=" + request.getOrderId() + "&status=cancelled")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency().toLowerCase())
                                                    .setUnitAmount(request.getAmount()*100)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + request.getOrderId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                   /* .setMetadata(Map.of(
                            "orderId", String.valueOf(request.getOrderId()),
                            "gateway", "STRIPE"
                    ))*/
                    .build();

            Session session = Session.create(params);
            log.info("Stripe session created: id={}, orderId={}, amount={}", session.getId(), request.getOrderId(), request.getAmount());

            return InitiatePaymentResponse.builder()
                    .paymentId(session.getId())
                    .providerOrderId(session.getId())
                    .redirectUrl(session.getUrl())
                    .gateway("STRIPE")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe API error: code={}, message={}", e.getCode(), e.getMessage());
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhook(String payload, String signatureHeader, Map<String, String> headers) {
        try {
            Webhook.constructEvent(payload, signatureHeader, stripeSecret);
            log.debug("Stripe webhook signature verified");
            return true;
        } catch (com.stripe.exception.SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Webhook verification error", e);
            return false;
        }
    }

}
