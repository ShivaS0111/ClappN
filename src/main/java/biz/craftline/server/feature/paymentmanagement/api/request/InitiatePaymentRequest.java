package biz.craftline.server.feature.paymentmanagement.api.request;

import lombok.*;

/**
 * Amounts are always in minor units (e.g. paise / cents).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentRequest {
    private Long orderId;
    /** Minor units (e.g. paise / cents) — do not send major currency units. */
    private Long amount;
    private String currency; // e.g. INR, USD
    private String gateway; // STRIPE or RAZORPAY
    private String callbackUrl; // success/cancel base URL for Stripe Checkout
}
