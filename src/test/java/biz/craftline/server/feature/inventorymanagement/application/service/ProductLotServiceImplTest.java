package biz.craftline.server.feature.inventorymanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.inventorymanagement.application.enums.TransactionType;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLot;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLotTransaction;
import biz.craftline.server.feature.inventorymanagement.domain.service.StoreInventoryService;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotTransactionEntity;
import biz.craftline.server.feature.inventorymanagement.infra.mapper.ProductLotEntityMapper;
import biz.craftline.server.feature.inventorymanagement.infra.mapper.ProductLotTransactionEntityMapper;
import biz.craftline.server.feature.inventorymanagement.infra.repository.ProductLotRepository;
import biz.craftline.server.feature.inventorymanagement.infra.repository.ProductLotTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductLotServiceImplTest {

    @Mock private ProductLotRepository lotRepository;
    @Mock private ProductLotTransactionRepository transactionRepository;
    @Mock private StoreInventoryService storeInventoryService;
    @Mock private SecurityContextService securityContextService;
    @Mock private ProductLotTransactionEntityMapper productLotTransactionEntityMapper;
    @Mock private ProductLotEntityMapper productLotEntityMapper;

    @InjectMocks
    private ProductLotServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(securityContextService.getAccessibleStoreIds()).thenReturn(null);
        doNothing().when(securityContextService).validateStoreAccess(any());
    }

    @Test
    void createLot_savesAndAddsStock() {
        ProductLot lot = ProductLot.builder().storeId(1L).productId(2L).quantity(10).build();
        ProductLotEntity entity = ProductLotEntity.builder().id(5L).storeId(1L).productId(2L).quantity(10).build();
        ProductLot domain = ProductLot.builder().id(5L).storeId(1L).productId(2L).quantity(10).build();

        when(productLotEntityMapper.toEntity(lot)).thenReturn(entity);
        when(lotRepository.save(entity)).thenReturn(entity);
        when(productLotEntityMapper.toDomain(entity)).thenReturn(domain);

        ProductLot result = service.createLot(lot);

        assertEquals(5L, result.getId());
        verify(storeInventoryService).addStock(1L, 2L, 10, "PURCHASE", "5", "Lot created");
        verify(securityContextService).validateStoreAccess(1L);
    }

    @Test
    void getLotById_returnsDomain() {
        ProductLotEntity entity = ProductLotEntity.builder().id(1L).storeId(1L).build();
        ProductLot domain = ProductLot.builder().id(1L).storeId(1L).build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productLotEntityMapper.toDomain(entity)).thenReturn(domain);

        assertEquals(domain, service.getLotById(1L));
        verify(securityContextService).validateStoreAccess(1L);
    }

    @Test
    void getLotById_throwsWhenMissing() {
        when(lotRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getLotById(9L));
    }

    @Test
    void getAllActiveLots_unrestricted_returnsAll() {
        ProductLotEntity e1 = ProductLotEntity.builder().id(1L).storeId(1L).build();
        ProductLotEntity e2 = ProductLotEntity.builder().id(2L).storeId(2L).build();
        when(lotRepository.findByProductIdAndActive(10L, 1)).thenReturn(List.of(e1, e2));
        when(productLotEntityMapper.toDomain(e1)).thenReturn(ProductLot.builder().id(1L).storeId(1L).build());
        when(productLotEntityMapper.toDomain(e2)).thenReturn(ProductLot.builder().id(2L).storeId(2L).build());

        assertEquals(2, service.getAllActiveLots(10L).size());
    }

    @Test
    void getAllActiveLots_filtersByAccessibleStores() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(1L));
        ProductLotEntity e1 = ProductLotEntity.builder().id(1L).storeId(1L).build();
        ProductLotEntity e2 = ProductLotEntity.builder().id(2L).storeId(2L).build();
        when(lotRepository.findByProductIdAndActive(10L, 1)).thenReturn(List.of(e1, e2));
        when(productLotEntityMapper.toDomain(e1)).thenReturn(ProductLot.builder().id(1L).storeId(1L).build());
        when(productLotEntityMapper.toDomain(e2)).thenReturn(ProductLot.builder().id(2L).storeId(2L).build());

        List<ProductLot> result = service.getAllActiveLots(10L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getStoreId());
    }

    @Test
    void updateLot_validatesExistingAndSaves() {
        ProductLot lot = ProductLot.builder().id(1L).storeId(1L).quantity(5).build();
        ProductLotEntity existing = ProductLotEntity.builder().id(1L).storeId(1L).build();
        ProductLotEntity saved = ProductLotEntity.builder().id(1L).storeId(1L).quantity(5).build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productLotEntityMapper.toEntity(lot)).thenReturn(saved);
        when(lotRepository.save(saved)).thenReturn(saved);
        when(productLotEntityMapper.toDomain(saved)).thenReturn(lot);

        assertEquals(lot, service.updateLot(lot));
        verify(securityContextService, atLeastOnce()).validateStoreAccess(1L);
    }

    @Test
    void deleteLot_returnsTrueWhenFound() {
        ProductLotEntity entity = ProductLotEntity.builder().id(1L).storeId(1L).build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertTrue(service.deleteLot(1L));
        verify(lotRepository).deleteById(1L);
    }

    @Test
    void deleteLot_returnsFalseWhenMissing() {
        when(lotRepository.findById(1L)).thenReturn(Optional.empty());
        assertFalse(service.deleteLot(1L));
    }

    @Test
    void recordTransaction_block() {
        ProductLotEntity lot = ProductLotEntity.builder()
                .id(1L).storeId(1L).productId(2L).quantity(10).blocked(0).sold(0).unitPrice(5.0).build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(lotRepository.save(lot)).thenReturn(lot);
        ProductLotTransactionEntity txEntity = ProductLotTransactionEntity.builder().id(100L).build();
        when(transactionRepository.save(any())).thenReturn(txEntity);
        ProductLotTransaction tx = ProductLotTransaction.builder().id(100L).build();
        when(productLotTransactionEntityMapper.toDomain(txEntity)).thenReturn(tx);

        ProductLotTransaction result = service.recordTransaction(1L, TransactionType.BLOCK, 2, "r", "ref", 9L);

        assertEquals(100L, result.getId());
        assertEquals(2, lot.getBlocked());
        verify(storeInventoryService).adjustBlocked(1L, 2L, 2, "BLOCK");
    }

    @Test
    void recordTransaction_sold() {
        ProductLotEntity lot = ProductLotEntity.builder()
                .id(1L).storeId(1L).productId(2L).quantity(10).blocked(0).sold(0).unitPrice(5.0).build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(lotRepository.save(lot)).thenReturn(lot);
        ProductLotTransactionEntity txEntity = ProductLotTransactionEntity.builder().id(101L).build();
        when(transactionRepository.save(any())).thenReturn(txEntity);
        when(productLotTransactionEntityMapper.toDomain(txEntity))
                .thenReturn(ProductLotTransaction.builder().id(101L).build());

        service.recordTransaction(1L, TransactionType.SOLD, 3, "sale", "ord-1", 1L);
        assertEquals(3, lot.getSold());
        verify(storeInventoryService).adjustForSale(1L, 2L, 3, "ORDER", "ord-1", "sale");
    }

    @Test
    void productLotBlock_delegatesToRecordTransaction() {
        ProductLotEntity lot = ProductLotEntity.builder()
                .id(1L).storeId(1L).productId(2L).quantity(10).blocked(0).sold(0).unitPrice(1.0).build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(lotRepository.save(lot)).thenReturn(lot);
        when(transactionRepository.save(any())).thenReturn(ProductLotTransactionEntity.builder().id(1L).build());
        when(productLotTransactionEntityMapper.toDomain(any()))
                .thenReturn(ProductLotTransaction.builder().id(1L).build());

        service.productLotBlock(1L, 2, "ALLOC", 50L);
        assertEquals(2, lot.getBlocked());
    }

    @Test
    void getTransactionsForLot_mapsList() {
        ProductLotEntity lot = ProductLotEntity.builder().id(1L).storeId(1L).build();
        ProductLotTransactionEntity tx = ProductLotTransactionEntity.builder().id(7L).build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(transactionRepository.findByProductLot(lot)).thenReturn(List.of(tx));
        when(productLotTransactionEntityMapper.toDomain(tx))
                .thenReturn(ProductLotTransaction.builder().id(7L).build());

        assertEquals(1, service.getTransactionsForLot(1L).size());
    }

    @Test
    void findByStoreIdAndProductIdAndActiveTrue() {
        ProductLotEntity entity = ProductLotEntity.builder().id(1L).storeId(1L).productId(2L).build();
        when(lotRepository.findByStoreIdAndProductIdAndActive(1L, 2L, 1)).thenReturn(List.of(entity));
        when(productLotEntityMapper.toDomain(entity))
                .thenReturn(ProductLot.builder().id(1L).storeId(1L).productId(2L).build());

        assertEquals(1, service.findByStoreIdAndProductIdAndActiveTrue(1L, 2L).size());
    }
}
