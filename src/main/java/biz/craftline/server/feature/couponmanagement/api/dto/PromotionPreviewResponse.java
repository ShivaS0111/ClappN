package biz.craftline.server.feature.couponmanagement.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PromotionPreviewResponse {
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal billDiscount;
    private String billDiscountType;
    private String couponCode;
    private BigDecimal payable;
    private List<Long> appliedPromotionIds = new ArrayList<>();
    private List<LineDiscount> lineDiscounts = new ArrayList<>();

    @Data
    public static class LineDiscount {
        private Long itemId;
        private Long itemType;
        private BigDecimal discountAmount;
    }
}
