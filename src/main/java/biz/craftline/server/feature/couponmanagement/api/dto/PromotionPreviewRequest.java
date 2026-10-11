package biz.craftline.server.feature.couponmanagement.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PromotionPreviewRequest {
    private Long storeId;
    private Long businessId;
    private Long userId;
    private String couponCode;
    private List<PreviewLine> items = new ArrayList<>();

    @Data
    public static class PreviewLine {
        private Long itemType;
        private Long itemId;
        private int quantity;
        private Double unitPrice;
    }
}
