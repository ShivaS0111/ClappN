package biz.craftline.server.feature.couponmanagement.application.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PromotionApplyResult {
    private BigDecimal subtotal;
    private BigDecimal totalLineDiscount;
    private BigDecimal billDiscount;
    private String billDiscountType;
    private String couponCode;
    private BigDecimal totalDiscount;
    private BigDecimal payable;
    @Builder.Default
    private List<Long> appliedPromotionIds = new ArrayList<>();
    /** Parallel to context lines — discount per line after apply. */
    @Builder.Default
    private List<BigDecimal> lineDiscounts = new ArrayList<>();
}
