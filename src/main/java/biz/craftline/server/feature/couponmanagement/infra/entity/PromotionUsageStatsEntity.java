package biz.craftline.server.feature.couponmanagement.infra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "promotion_usage_stats")
public class PromotionUsageStatsEntity {

    @Id
    @Column(name = "promotion_id")
    private Long promotionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "promotion_id")
    private PromotionEntity promotion;

    @Column(name = "redemption_count", nullable = false)
    @Builder.Default
    private Long redemptionCount = 0L;

    @Column(name = "total_discount_given", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalDiscountGiven = BigDecimal.ZERO;
}
