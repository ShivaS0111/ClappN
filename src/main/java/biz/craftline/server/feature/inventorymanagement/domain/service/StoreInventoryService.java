package biz.craftline.server.feature.inventorymanagement.domain.service;

import biz.craftline.server.feature.inventorymanagement.domain.model.StoreInventory;
import org.springframework.stereotype.Service;

@Service
public interface StoreInventoryService {
    StoreInventory addStock(Long storeId, Long productId, int quantity, String referenceType,
                            String referenceId, String reason);

    StoreInventory adjustForSale(Long storeId, Long productId, int quantity, String referenceType,
                                       String referenceId, String reason);

    StoreInventory adjustBlocked(Long storeId, Long productId, int blockedDelta, String reason);

}
