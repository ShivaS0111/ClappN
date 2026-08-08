package biz.craftline.server.feature.paymentmanagement.api.request;

import lombok.Data;

@Data
public class RefundPaymentRequest {
    /** Optional partial refund in minor units; null = full refund. */
    private Long amount;
}
