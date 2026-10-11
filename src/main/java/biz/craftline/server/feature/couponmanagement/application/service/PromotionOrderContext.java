package biz.craftline.server.feature.couponmanagement.application.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PromotionOrderContext {
    private Long businessId;
    private Long storeId;
    private Long userId;
    private String couponCode;
    @Builder.Default
    private List<Line> lines = new ArrayList<>();

    @Data
    @Builder
    public static class Line {
        private Long itemType;
        private Long itemId;
        private int quantity;
        private BigDecimal unitPrice;
        /** Mutable: accumulated line discount. */
        @Builder.Default
        private BigDecimal discountAmount = BigDecimal.ZERO;
    }
}
