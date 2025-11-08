package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
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
    @Query(value = """
            SELECT * FROM store_item_price p
            WHERE p.item_id = :itemId
              AND p.item_type = :itemType
              AND p.status = 1
              AND (p.valid_from IS NULL OR p.valid_from <= CURRENT_TIMESTAMP)
              AND (p.valid_to IS NULL OR p.valid_to >= CURRENT_TIMESTAMP)
            ORDER BY p.valid_from DESC NULLS LAST, p.created_at DESC
            """, nativeQuery = true)
    List<StoreItemPriceEntity> findActivePrices(
            @Param("itemType") Long itemType,
            @Param("itemId") Long itemId,
            Pageable pageable
    );


    // === BULK / QUANTITY-SPECIFIC PRICE ===
    @Query(value = """
            SELECT * FROM store_item_price p
            WHERE p.item_id = :itemId
              AND p.item_type = :itemType
              AND p.status = 1
              AND (p.valid_from IS NULL OR p.valid_from <= CURRENT_TIMESTAMP)
              AND (p.valid_to IS NULL OR p.valid_to >= CURRENT_TIMESTAMP)
              AND p.min_quantity <= :quantity
            ORDER BY p.min_quantity DESC, p.valid_from DESC
            """, nativeQuery = true)
    List<StoreItemPriceEntity> findBestApplicablePrice(
            @Param("itemType") Long itemType,
            @Param("itemId") Long itemId,
            @Param("quantity") int quantity,
            Pageable pageable
    );


    // === LATEST (by createdAt) ===
    @Query(value = """
            SELECT * FROM store_item_price p
            WHERE p.item_id = :itemId
              AND p.item_type = :itemType
            ORDER BY p.created_at DESC
            """, nativeQuery = true)
    Optional<StoreItemPriceEntity> findLatestPriceByItem(
            @Param("itemType") Long itemType,
            @Param("itemId") Long itemId
    );


    // === CURRENT VALID PRICE (time-valid only) ===
    @Query(value = """
            SELECT * FROM store_item_price p 
            WHERE p.item_type = :itemType 
            AND p.item_id = :itemId 
            AND p.status = 1
            AND (p.valid_from IS NULL OR p.valid_from <= :now)
            AND (p.valid_to IS NULL OR p.valid_to >= :now)
            ORDER BY p.valid_from DESC, p.created_at DESC
            """, nativeQuery = true)
    Optional<StoreItemPriceEntity> findCurrentPriceByItem(
            @Param("itemType") Long itemType,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now
    );

    // 1️⃣ Get latest active lot for a given product
    @Query(value = """
        SELECT * FROM product_lot l
        WHERE l.product_id = :productId
          AND l.active = true
        ORDER BY l.purchased_at DESC
        """, nativeQuery = true)
    List<ProductLotEntity> findActiveLotsByProduct(@Param("productId") Long productId, Pageable pageable);

    // 2️⃣ Get latest price for that lot
    @Query(value = """
        SELECT * FROM store_item_price p
        WHERE p.product_lot_id = :productLotId
        ORDER BY p.created_at DESC
        """, nativeQuery = true)
    Optional<StoreItemPriceEntity> findLatestPriceByProductLot(@Param("productLotId") Long productLotId);

    // 3️⃣ Get currently active price (based on validity)
    @Query(value = """
        SELECT * FROM store_item_price p
        WHERE p.product_lot_id = :productLotId
          AND p.status = 1
          AND (p.valid_from IS NULL OR p.valid_from <= CURRENT_TIMESTAMP)
          AND (p.valid_to IS NULL OR p.valid_to >= CURRENT_TIMESTAMP)
        ORDER BY p.valid_from DESC NULLS LAST, p.created_at DESC
        """, nativeQuery = true)
    Optional<StoreItemPriceEntity> findCurrentPriceByProductLot(@Param("productLotId") Long productLotId);

    @Query(value = """
        SELECT p.* FROM store_item_price p
        JOIN product_lot pl ON p.product_lot_id = pl.id
        JOIN product prod ON pl.product_id = prod.id
        WHERE prod.id = :productId
        ORDER BY p.created_at DESC
        """, nativeQuery = true)
    Optional<StoreItemPriceEntity> findLatestPriceByProductId(@Param("productId") Long productId);

    @Query(value = """
        SELECT p.* FROM store_item_price p
        JOIN product_lot pl ON p.product_lot_id = pl.id
        JOIN product prod ON pl.product_id = prod.id
        WHERE prod.id = :productId
          AND p.status = 1
          AND (p.valid_from IS NULL OR p.valid_from <= :now)
          AND (p.valid_to IS NULL OR p.valid_to >= :now)
        ORDER BY p.created_at DESC
        """, nativeQuery = true)
    Optional<StoreItemPriceEntity> findCurrentPriceByProductId(
            @Param("productId") Long productId,
            @Param("now") LocalDateTime now);
}
