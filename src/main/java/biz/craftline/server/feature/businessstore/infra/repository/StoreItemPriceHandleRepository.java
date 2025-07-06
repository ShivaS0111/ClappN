package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreItemPriceHandleRepository extends JpaRepository<StoreItemPriceEntity, Long> {

    //List<StoreItemPriceEntity> findAllByItemIdAndItemType(Long itemId, Long itemType);

    /*@Query("""
        SELECT p FROM store_product_price p
        WHERE p.product_id = :productId
        AND p.country_id = :countryCode
        AND p.validTo IS NULL
    """)
    Optional<StoreProductPriceEntity> findCurrentPrice(@Param("productId") Long productId,
                                                       @Param("countryCode") String countryCode);*/


    List<StoreItemPriceEntity> findAllByServiceId(Long serviceId);
    List<StoreItemPriceEntity> findAllByProductLotId(Long productLotId);
    //List<StoreItemPriceEntity> findAllByProductId(Long productLotId);

    Optional<StoreItemPriceEntity> findByServiceId( Long serviceId );
    //Optional<StoreItemPriceEntity> findByProductId( Long itemId );
    Optional<StoreItemPriceEntity> findByProductLotId( Long productLotId );


}
