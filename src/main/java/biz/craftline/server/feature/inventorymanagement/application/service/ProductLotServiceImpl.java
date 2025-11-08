package biz.craftline.server.feature.inventorymanagement.application.service;

import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLot;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLotTransaction;
import biz.craftline.server.feature.inventorymanagement.domain.service.ProductLotService;
import biz.craftline.server.feature.inventorymanagement.domain.service.StoreInventoryService;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotTransactionEntity;
import biz.craftline.server.feature.inventorymanagement.infra.mapper.ProductLotEntityMapper;
import biz.craftline.server.feature.inventorymanagement.infra.mapper.ProductLotTransactionEntityMapper;
import biz.craftline.server.feature.inventorymanagement.infra.repository.ProductLotRepository;
import biz.craftline.server.feature.inventorymanagement.infra.repository.ProductLotTransactionRepository;
import biz.craftline.server.feature.inventorymanagement.application.enums.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductLotServiceImpl implements ProductLotService {

    private final ProductLotRepository lotRepository;
    private final ProductLotTransactionRepository transactionRepository;
    private final StoreInventoryService storeInventoryService;

    private final ProductLotTransactionEntityMapper productLotTransactionEntityMapper;
    private final ProductLotEntityMapper productLotEntityMapper;

    @Override
    public ProductLot createLot(ProductLot lot) {

        ProductLotEntity lotEntity = productLotEntityMapper.toEntity(lot);
        ProductLotEntity savedLotEntity= lotRepository.save(lotEntity);

        storeInventoryService.addStock(
                savedLotEntity.getStoreId(),
                savedLotEntity.getProductId(),
                lot.getQuantity(),
                "PURCHASE",
                String.valueOf(savedLotEntity.getId()),
                "Lot created");
        return productLotEntityMapper.toDomain( savedLotEntity );
    }

    @Override
    public ProductLot getLotById(Long id) {
        ProductLotEntity lotEntity = lotRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Lot not found for id:"+id));

        return productLotEntityMapper.toDomain( lotEntity );
    }

    @Override
    public List<ProductLot> getAllActiveLots() {
        return lotRepository.findByActiveTrue()
                .stream()
                .map(productLotEntityMapper::toDomain)
                .toList();
    }

    @Override
    public ProductLot updateLot(ProductLot lot) {
        ProductLotEntity lotEntity = productLotEntityMapper.toEntity(lot);
        ProductLotEntity savedLotEntity= lotRepository.save(lotEntity);
        return productLotEntityMapper.toDomain(savedLotEntity);
    }

    @Override
    public boolean deleteLot(Long id) {
        if (lotRepository.existsById(id)) {
            lotRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public ProductLotTransaction recordTransaction(Long lotId, TransactionType type, int quantityChange,
                                                   String reason, String referenceId, long performedBy) {
        ProductLotEntity lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found: " + lotId));

        int before = lot.getAvailable();

        switch (type) {
            case BLOCK -> {
                lot.setBlocked(lot.getBlocked() + quantityChange);
                storeInventoryService.adjustBlocked(lot.getStoreId(), lot.getProductId(), quantityChange, "BLOCK");
            }
            case UNBLOCK -> {
                lot.setBlocked(lot.getBlocked() - quantityChange);
                storeInventoryService.adjustBlocked(lot.getStoreId(), lot.getProductId(), -quantityChange, "UNBLOCK");
            }
            case SOLD -> {
                lot.setSold(lot.getSold() + quantityChange);
                storeInventoryService.adjustForSale(lot.getStoreId(), lot.getProductId(), quantityChange, "ORDER", referenceId, reason);
            }
            case RETURN -> {
                lot.setSold(lot.getSold() - quantityChange);
                storeInventoryService.addStock(lot.getStoreId(), lot.getProductId(), quantityChange, "RETURN", referenceId, reason);
            }
            case ADJUST -> {
                lot.setQuantity(lot.getQuantity() + quantityChange);
                storeInventoryService.addStock(lot.getStoreId(), lot.getProductId(), quantityChange, "ADJUST", referenceId, reason);
            }
            default -> throw new IllegalArgumentException("Invalid transaction type");
        }


        int after = lot.getAvailable();
        lotRepository.save(lot);

        ProductLotTransactionEntity tx = ProductLotTransactionEntity.builder()
                .productLot(lot)
                .type(type)
                .quantityChange(quantityChange)
                .beforeQuantity(before)
                .afterQuantity(after)
                .unitPrice(BigDecimal.valueOf(lot.getUnitPrice()))
                .reason(reason)
                .referenceId(referenceId)
                .performedBy(performedBy)
                .correlationId("CORR-" + System.currentTimeMillis())
                .createdAt(LocalDateTime.now())
                .build();

        ProductLotTransactionEntity transactionEntity = transactionRepository.save(tx);
        return productLotTransactionEntityMapper.toDomain( transactionEntity );
    }


    @Override
    @Transactional
    public List<ProductLotTransaction> getTransactionsForLot(Long lotId) {
        ProductLotEntity lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));
        return transactionRepository.findByProductLot(lot).stream()
                .map(productLotTransactionEntityMapper::toDomain)
                .toList();
    }

}