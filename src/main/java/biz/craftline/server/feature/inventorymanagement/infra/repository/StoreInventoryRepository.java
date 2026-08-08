package biz.craftline.server.feature.inventorymanagement.infra.repository;

import biz.craftline.server.feature.inventorymanagement.infra.entity.StoreInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreInventoryRepository extends JpaRepository<StoreInventoryEntity, Long> {

    Optional<StoreInventoryEntity> findByStoreIdAndProductId(Long storeId, Long productId);

    List<StoreInventoryEntity> findByStoreId(Long storeId);

    @Query("SELECT e FROM StoreInventoryEntity e WHERE e.storeId = :storeId AND e.available < :threshold")
    List<StoreInventoryEntity> findLowStockByStoreId(@Param("storeId") Long storeId, @Param("threshold") int threshold);

    @Query("SELECT COUNT(e) FROM StoreInventoryEntity e WHERE e.storeId = :storeId AND e.available < :threshold")
    long countLowStockByStoreId(@Param("storeId") Long storeId, @Param("threshold") int threshold);

}
