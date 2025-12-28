package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.api.dto.ItemKey;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
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

    @Query("""
        SELECT p
        FROM store_item_price p
        WHERE p.itemId IN :itemIds
          AND p.itemType = :itemType
          AND p.status = 1
          AND (p.validFrom IS NULL OR p.validFrom <= CURRENT_TIMESTAMP)
          AND (p.validTo IS NULL OR p.validTo >= CURRENT_TIMESTAMP)
        ORDER BY p.validFrom DESC NULLS LAST, p.createdAt DESC
    """)
    List<StoreItemPriceEntity> findLatestPricesForItems(
            @Param("itemIds") List<Long> itemIds,
            @Param("itemType") Long itemType
    );


}
