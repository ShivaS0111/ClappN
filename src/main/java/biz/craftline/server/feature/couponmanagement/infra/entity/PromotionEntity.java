package biz.craftline.server.feature.couponmanagement.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "promotion")
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 64)
    private String code;

    @Column(name = "benefit_type", nullable = false, length = 32)
    private String benefitType;

    @Column(name = "application_level", nullable = false, length = 16)
    private String applicationLevel;

    @Column(name = "benefit_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal benefitValue;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "min_cart_amount", precision = 19, scale = 4)
    private BigDecimal minCartAmount;

    @Column(name = "max_discount_amount", precision = 19, scale = 4)
    private BigDecimal maxDiscountAmount;

    @Column(name = "max_global_redemptions")
    private Integer maxGlobalRedemptions;

    @Column(name = "max_per_user")
    private Integer maxPerUser;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = "DRAFT";

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 100;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromotionTargetEntity> targets = new ArrayList<>();

    @OneToOne(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PromotionUsageStatsEntity usageStats;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
