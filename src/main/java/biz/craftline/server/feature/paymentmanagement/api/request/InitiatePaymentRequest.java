package biz.craftline.server.feature.paymentmanagement.api.request;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InitiatePaymentRequest {
    private Long orderId;
    private Long amount; // minor units (e.g. paise)
    private String currency; // e.g. INR
    private String gateway; // STRIPE or RAZORPAY
    private String callbackUrl; // e.g., "https://yourapp.com/order/123/payment-result"
}
