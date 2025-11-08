package biz.craftline.server.feature.inventorymanagement.infra.repository;

import biz.craftline.server.feature.inventorymanagement.infra.entity.StoreInventoryTransactionEntity;
import biz.craftline.server.feature.inventorymanagement.infra.entity.StoreInventoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<StoreInventoryTransactionEntity, Long> {

    List<StoreInventoryTransactionEntity> findByInventory(StoreInventoryEntity inventory);

}
