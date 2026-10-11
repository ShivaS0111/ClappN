package biz.craftline.server.feature.couponmanagement.domain.model;

import lombok.Data;

@Data
public class PromotionTarget {
    private String targetType;
    private Long targetId;
}
