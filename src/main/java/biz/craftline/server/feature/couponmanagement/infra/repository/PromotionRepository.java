package biz.craftline.server.feature.couponmanagement.infra.repository;

import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    List<PromotionEntity> findByBusinessId(Long businessId);

    List<PromotionEntity> findByStoreId(Long storeId);

    Optional<PromotionEntity> findByBusinessIdAndCodeIgnoreCase(Long businessId, String code);

    @Query("""
            SELECT DISTINCT p FROM PromotionEntity p
            LEFT JOIN FETCH p.targets
            LEFT JOIN FETCH p.usageStats
            WHERE p.status = :status
              AND p.businessId = :businessId
              AND (p.storeId IS NULL OR p.storeId = :storeId)
            """)
    List<PromotionEntity> findActiveForStore(@Param("businessId") Long businessId,
                                             @Param("storeId") Long storeId,
                                             @Param("status") String status);
}
