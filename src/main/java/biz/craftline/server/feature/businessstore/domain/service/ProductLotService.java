package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotTransaction;
import biz.craftline.server.util.TransactionType;

import java.util.List;
import java.util.Optional;

public interface ProductLotService {

    ProductLotEntity createLot(ProductLotEntity lot);

    Optional<ProductLotEntity> getLotById(Long id);

    List<ProductLotEntity> getAllActiveLots();

    ProductLotEntity updateLot(ProductLotEntity lot);

    boolean deleteLot(Long id);

    // Transactional operations
    ProductLotTransaction recordTransaction(Long lotId, TransactionType type, int quantityChange, String reason,
                                            String referenceId, long performedBy);

    List<ProductLotTransaction> getTransactionsForLot(Long lotId);
}