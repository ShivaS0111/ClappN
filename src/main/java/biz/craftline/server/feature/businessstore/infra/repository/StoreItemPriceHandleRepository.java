package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreItemPriceHandleRepository extends JpaRepository<StoreItemPriceEntity, Long> {

    // === BASIC LOOKUPS ===
    List<StoreItemPriceEntity> findAllByItemIdAndItemType(Long itemId, Long itemType);

    List<StoreItemPriceEntity> findAllByItemType(Long itemType);

    List<StoreItemPriceEntity> findByItemIdAndItemTypeOrderByIdDesc(Long itemId, Long itemType);

    Optional<StoreItemPriceEntity> findByItemIdAndItemType(Long itemId, Long itemType);


    // === ACTIVE PRICES (time-valid and active) ===
    @Query("""
            SELECT p FROM StoreItemPriceEntity p
            WHERE p.itemId = :itemId
              AND p.itemType = :itemType
              AND p.status = 1
              AND (p.validFrom IS NULL OR p.validFrom <= CURRENT_TIMESTAMP)
              AND (p.validTo IS NULL OR p.validTo >= CURRENT_TIMESTAMP)
            ORDER BY p.validFrom DESC NULLS LAST, p.createdAt DESC
            """)
    List<StoreItemPriceEntity> findActivePrices(
            @Param("itemType") Long itemType,
            @Param("itemId") Long itemId,
            Pageable pageable
    );


    // === BULK / QUANTITY-SPECIFIC PRICE ===
    @Query("""
            SELECT p FROM StoreItemPriceEntity p
            WHERE p.itemId = :itemId
              AND p.itemType = :itemType
              AND p.status = 1
              AND (p.validFrom IS NULL OR p.validFrom <= CURRENT_TIMESTAMP)
              AND (p.validTo IS NULL OR p.validTo >= CURRENT_TIMESTAMP)
              AND p.minQuantity <= :quantity
            ORDER BY p.minQuantity DESC, p.validFrom DESC
            """)
    List<StoreItemPriceEntity> findBestApplicablePrice(
            @Param("itemType") Long itemType,
            @Param("itemId") Long itemId,
            @Param("quantity") int quantity,
            Pageable pageable
    );


    // === LATEST (by createdAt) ===
    @Query("""
            SELECT p FROM StoreItemPriceEntity p
            WHERE p.itemId = :itemId
              AND p.itemType = :itemType
            ORDER BY p.createdAt DESC
            """)
    Optional<StoreItemPriceEntity> findLatestPriceByItem(
            @Param("itemType") Long itemType,
            @Param("itemId") Long itemId
    );


    // === CURRENT VALID PRICE (time-valid only) ===
    @Query("""
            SELECT p FROM StoreItemPriceEntity p
            WHERE p.itemId = :itemId
              AND p.itemType = :itemType
              AND p.validFrom <= :now
              AND (p.validTo IS NULL OR p.validTo >= :now)
              AND p.status = 1
            ORDER BY p.validFrom DESC
            """)
    Optional<StoreItemPriceEntity> findCurrentPriceByItem(
            @Param("itemType") Long itemType,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now
    );

    // 1️⃣ Get latest active lot for a given product
    @Query("""
        SELECT l FROM ProductLotEntity l
        WHERE l.product.id = :productId
          AND l.active = true
        ORDER BY l.purchasedAt DESC
        """)
    List<ProductLotEntity> findActiveLotsByProduct(@Param("productId") Long productId, Pageable pageable);

    // 2️⃣ Get latest price for that lot
    @Query("""
        SELECT p FROM StoreItemPriceEntity p
        WHERE p.productLot.id = :productLotId
        ORDER BY p.createdAt DESC
        """)
    Optional<StoreItemPriceEntity> findLatestPriceByProductLot(@Param("productLotId") Long productLotId);

    // 3️⃣ Get currently active price (based on validity)
    @Query("""
        SELECT p FROM StoreItemPriceEntity p
        WHERE p.productLot.id = :productLotId
          AND p.status = 1
          AND (p.validFrom IS NULL OR p.validFrom <= CURRENT_TIMESTAMP)
          AND (p.validTo IS NULL OR p.validTo >= CURRENT_TIMESTAMP)
        ORDER BY p.validFrom DESC NULLS LAST, p.createdAt DESC
        """)
    Optional<StoreItemPriceEntity> findCurrentPriceByProductLot(@Param("productLotId") Long productLotId);

    @Query("""
    SELECT sip
    FROM StoreItemPriceEntity sip
    JOIN sip.productLot pl
    JOIN pl.product p
    WHERE p.id = :productId
    ORDER BY sip.createdAt DESC
    """)
    Optional<StoreItemPriceEntity> findLatestPriceByProductId(@Param("productId") Long productId);

    @Query("""
    SELECT sip
    FROM StoreItemPriceEntity sip
    JOIN sip.productLot pl
    JOIN pl.product p
    WHERE p.id = :productId
      AND sip.status = 1
      AND (sip.validFrom IS NULL OR sip.validFrom <= :now)
      AND (sip.validTo IS NULL OR sip.validTo >= :now)
    ORDER BY sip.createdAt DESC
    """)
    Optional<StoreItemPriceEntity> findCurrentPriceByProductId(
            @Param("productId") Long productId,
            @Param("now") LocalDateTime now);
}
