package biz.craftline.server.feature.inventorymanagement.application.service;


import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.inventorymanagement.domain.model.StoreInventory;
import biz.craftline.server.feature.inventorymanagement.domain.service.StoreInventoryService;
import biz.craftline.server.feature.inventorymanagement.infra.entity.StoreInventoryTransactionEntity;
import biz.craftline.server.feature.inventorymanagement.infra.entity.StoreInventoryEntity;
import biz.craftline.server.feature.inventorymanagement.infra.mapper.StoreInventoryEntityMapper;
import biz.craftline.server.feature.inventorymanagement.infra.repository.InventoryTransactionRepository;
import biz.craftline.server.feature.inventorymanagement.infra.repository.StoreInventoryRepository;
import biz.craftline.server.feature.inventorymanagement.application.enums.InventoryAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StoreInventoryServiceImpl implements StoreInventoryService {

    private final StoreInventoryRepository storeInventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final StoreInventoryEntityMapper storeInventoryEntityMapper;
    private final SecurityContextService securityContextService;

    @Override
    public List<StoreInventory> findByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        return storeInventoryRepository.findByStoreId(storeId)
                .stream()
                .map(storeInventoryEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoreInventory addStock(Long storeId, Long productId, int quantity, String referenceType,
                                   String referenceId, String reason) {
        securityContextService.validateStoreAccess(storeId);
        StoreInventoryEntity inv = storeInventoryRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseGet(() -> createInventory(storeId, productId));


        inv.setTotalQuantity(inv.getTotalQuantity() + quantity);
        inv.setAvailable(inv.getAvailable() + quantity);

        StoreInventoryEntity storeInventoryEntity = storeInventoryRepository.save(inv);

        inventoryTransactionRepository.save(StoreInventoryTransactionEntity.builder()
                .inventory(storeInventoryEntity)
                .action(InventoryAction.IN)
                .quantityChange(quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .reason(reason)
                .build());


        return storeInventoryEntityMapper.toDomain( storeInventoryRepository.save(inv) );
    }


    @Transactional
    public StoreInventory adjustForSale(Long storeId, Long productId, int quantity, String referenceType,
                                        String referenceId, String reason) {
        securityContextService.validateStoreAccess(storeId);
        StoreInventoryEntity inv = storeInventoryRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseThrow(() -> new RuntimeException("Store inventory not found"));


        if (inv.getAvailable() < quantity) throw new IllegalArgumentException("Insufficient stock");


        inv.setAvailable(inv.getAvailable() - quantity);
        inv.setSold(inv.getSold() + quantity);


        inventoryTransactionRepository.save(StoreInventoryTransactionEntity.builder()
                .inventory(inv)
                .action(InventoryAction.OUT)
                .quantityChange(-quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .reason(reason)
                .build());


        return storeInventoryEntityMapper.toDomain( storeInventoryRepository.save(inv));
    }


    @Transactional
    public StoreInventory adjustBlocked(Long storeId, Long productId, int blockedDelta, String reason) {
        securityContextService.validateStoreAccess(storeId);
        StoreInventoryEntity inv = storeInventoryRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseGet(() -> createInventory(storeId, productId));


        inv.setBlocked(inv.getBlocked() + blockedDelta);
        inv.setAvailable(inv.getAvailable() - blockedDelta);


        inventoryTransactionRepository.save(StoreInventoryTransactionEntity.builder()
                .inventory(inv)
                .action(InventoryAction.ADJUST)
                .quantityChange(blockedDelta)
                .reason(reason)
                .build());


        return storeInventoryEntityMapper.toDomain( storeInventoryRepository.save(inv));
    }

    private StoreInventoryEntity createInventory(Long storeId, Long productId) {
        return StoreInventoryEntity.builder()
                .storeId(storeId)
                .productId(productId)
                .build();
    }
}
