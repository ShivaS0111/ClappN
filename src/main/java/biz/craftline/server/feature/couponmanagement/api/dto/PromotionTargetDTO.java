package biz.craftline.server.feature.couponmanagement.api.dto;

import lombok.Data;

@Data
public class PromotionTargetDTO {
    private String targetType;
    private Long targetId;
}
