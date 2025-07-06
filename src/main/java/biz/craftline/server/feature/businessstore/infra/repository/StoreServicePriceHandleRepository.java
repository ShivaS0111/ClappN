package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.StoreProductPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreServicePriceHandleRepository extends JpaRepository<StoreProductPriceEntity, Long> {

    List<StoreProductPriceEntity> findAllByProductIdAndProductType(Long productId, Long productType);

    @Query("""
        SELECT p FROM ProductPrice p
        WHERE p.productId = :productId
        AND p.countryCode = :countryCode
        AND p.validTo IS NULL
    """)
    Optional<StoreProductPriceEntity> findCurrentPrice(@Param("productId") Long productId,
                                                       @Param("countryCode") String countryCode);

}
