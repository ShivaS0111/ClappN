package biz.craftline.server.feature.couponmanagement.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionUsageResponse {
    private Long promotionId;
    private String name;
    private String code;
    private String benefitType;
    private String applicationLevel;
    private BigDecimal configuredBenefitValue;
    private BigDecimal maxDiscountAmount;
    private Integer maxGlobalRedemptions;
    private Integer maxPerUser;
    private String status;
    private Long redemptionCount;
    private BigDecimal totalDiscountGiven;
    private Long remainingRedemptions;
    private BigDecimal remainingBudget;
}
