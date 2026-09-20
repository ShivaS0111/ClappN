package biz.craftline.server.feature.paymentmanagement.api.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentResponse {
    /** Stripe Checkout Session id, or Razorpay order id (until payment id arrives via webhook). */
    private String paymentId;
    private String providerOrderId;
    /** Stripe Checkout URL; null for Razorpay (use clientKey + providerOrderId in Checkout.js). */
    private String redirectUrl;
    private String gateway;
    /** Stripe publishable key or Razorpay key_id for client-side checkout. */
    private String clientKey;
}
