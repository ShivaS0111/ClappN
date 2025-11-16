package biz.craftline.server.feature.inventorymanagement.domain.service;

import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLot;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLotTransaction;
import biz.craftline.server.feature.inventorymanagement.application.enums.TransactionType;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;

import java.util.List;

public interface ProductLotService {

    ProductLot createLot(ProductLot lot);

    ProductLot getLotById(Long id);

    List<ProductLot> getAllActiveLots();

    ProductLot updateLot(ProductLot lot);

    boolean deleteLot(Long id);

    // Transactional operations
    ProductLotTransaction recordTransaction(Long lotId, TransactionType type, int quantityChange, String reason,
                                            String referenceId, long performedBy);

    List<ProductLotTransaction> getTransactionsForLot(Long lotId);


    List<ProductLot> findByStoreIdAndProductIdAndActiveTrue(Long storeId, Long productId);

    void productLotBlock(Long lotId, int allocate, String orderAlloc, Long orderItemId);
}