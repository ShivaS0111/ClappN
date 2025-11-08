package biz.craftline.server.feature.inventorymanagement.infra.repository;

import biz.craftline.server.feature.inventorymanagement.infra.entity.StoreInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreInventoryRepository extends JpaRepository<StoreInventoryEntity, Long> {

    Optional<StoreInventoryEntity> findByStoreIdAndProductId(Long storeId, Long productId);

}
