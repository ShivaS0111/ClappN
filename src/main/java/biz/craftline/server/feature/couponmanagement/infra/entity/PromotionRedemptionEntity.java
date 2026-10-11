package biz.craftline.server.feature.couponmanagement.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "promotion_redemption")
public class PromotionRedemptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "redeemed_at", updatable = false)
    private Timestamp redeemedAt;
}
