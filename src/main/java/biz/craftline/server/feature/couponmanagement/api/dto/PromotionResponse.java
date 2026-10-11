package biz.craftline.server.feature.couponmanagement.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PromotionResponse {
    private Long id;
    private String name;
    private String code;
    private String benefitType;
    private String applicationLevel;
    private BigDecimal benefitValue;
    private Long businessId;
    private Long storeId;
    private BigDecimal minCartAmount;
    private BigDecimal maxDiscountAmount;
    private Integer maxGlobalRedemptions;
    private Integer maxPerUser;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
    private Integer priority;
    private List<PromotionTargetDTO> targets = new ArrayList<>();
    private Long redemptionCount;
    private BigDecimal totalDiscountGiven;
}
