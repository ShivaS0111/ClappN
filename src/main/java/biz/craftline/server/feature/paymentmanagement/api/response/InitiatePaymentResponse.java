package biz.craftline.server.feature.paymentmanagement.api.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InitiatePaymentResponse {
    private String paymentId;
    private String providerOrderId;
    private String redirectUrl;
    private String gateway;
}
