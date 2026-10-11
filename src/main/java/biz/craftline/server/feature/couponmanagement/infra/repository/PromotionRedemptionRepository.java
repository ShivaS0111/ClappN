package biz.craftline.server.feature.couponmanagement.infra.repository;

import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionRedemptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRedemptionRepository extends JpaRepository<PromotionRedemptionEntity, Long> {

    long countByPromotionIdAndUserId(Long promotionId, Long userId);
}
