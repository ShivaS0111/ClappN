package biz.craftline.server.feature.couponmanagement.infra.repository;

import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionUsageStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionUsageStatsRepository extends JpaRepository<PromotionUsageStatsEntity, Long> {
}
