package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.feature.ordermanagement.api.dto.DiscountMismatchDetails;
import lombok.Getter;

/**
 * Client discount/total claims do not match server recomputation.
 * Carries structured details for FE confirmation / reconfigure UX.
 */
@Getter
public class DiscountMismatchException extends RuntimeException {

    private final DiscountMismatchDetails details;

    public DiscountMismatchException(DiscountMismatchDetails details) {
        super(details != null ? details.getReason() : "Discount mismatch");
        this.details = details;
    }
}
