package biz.craftline.server.feature.inventorymanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.inventorymanagement.domain.model.StoreInventory;
import biz.craftline.server.feature.inventorymanagement.infra.entity.StoreInventoryEntity;
import biz.craftline.server.feature.inventorymanagement.infra.mapper.StoreInventoryEntityMapper;
import biz.craftline.server.feature.inventorymanagement.infra.repository.InventoryTransactionRepository;
import biz.craftline.server.feature.inventorymanagement.infra.repository.StoreInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class StoreInventoryServiceImplTest {

    @Mock private StoreInventoryRepository storeInventoryRepository;
    @Mock private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock private StoreInventoryEntityMapper storeInventoryEntityMapper;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private StoreInventoryServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(securityContextService).validateStoreAccess(anyLong());
    }

    @Test
    void findByStoreId_mapsEntities() {
        StoreInventoryEntity entity = StoreInventoryEntity.builder().id(1L).storeId(1L).productId(2L).build();
        StoreInventory domain = StoreInventory.builder().id(1L).storeId(1L).productId(2L).build();
        when(storeInventoryRepository.findByStoreId(1L)).thenReturn(List.of(entity));
        when(storeInventoryEntityMapper.toDomain(entity)).thenReturn(domain);

        List<StoreInventory> result = service.findByStoreId(1L);
        assertEquals(1, result.size());
        verify(securityContextService).validateStoreAccess(1L);
    }

    @Test
    void findLowStockByStoreId() {
        StoreInventoryEntity entity = StoreInventoryEntity.builder().id(1L).storeId(1L).available(2).build();
        when(storeInventoryRepository.findLowStockByStoreId(1L, 5)).thenReturn(List.of(entity));
        when(storeInventoryEntityMapper.toDomain(entity))
                .thenReturn(StoreInventory.builder().id(1L).available(2).build());

        assertEquals(1, service.findLowStockByStoreId(1L, 5).size());
    }

    @Test
    void countLowStockByStoreId() {
        when(storeInventoryRepository.countLowStockByStoreId(1L, 5)).thenReturn(3L);
        assertEquals(3L, service.countLowStockByStoreId(1L, 5));
    }

    @Test
    void addStock_createsInventoryWhenMissing() {
        when(storeInventoryRepository.findByStoreIdAndProductId(1L, 2L)).thenReturn(Optional.empty());
        when(storeInventoryRepository.save(any(StoreInventoryEntity.class))).thenAnswer(inv -> {
            StoreInventoryEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        when(storeInventoryEntityMapper.toDomain(any(StoreInventoryEntity.class)))
                .thenReturn(StoreInventory.builder().id(10L).storeId(1L).productId(2L).available(5).build());

        StoreInventory result = service.addStock(1L, 2L, 5, "PURCHASE", "ref", "reason");
        assertEquals(10L, result.getId());
        verify(inventoryTransactionRepository).save(any());
    }

    @Test
    void addStock_updatesExisting() {
        StoreInventoryEntity inv = StoreInventoryEntity.builder()
                .id(1L).storeId(1L).productId(2L).totalQuantity(10).available(10).build();
        when(storeInventoryRepository.findByStoreIdAndProductId(1L, 2L)).thenReturn(Optional.of(inv));
        when(storeInventoryRepository.save(inv)).thenReturn(inv);
        when(storeInventoryEntityMapper.toDomain(inv))
                .thenReturn(StoreInventory.builder().id(1L).available(15).build());

        StoreInventory result = service.addStock(1L, 2L, 5, "PURCHASE", "ref", "reason");
        assertEquals(15, inv.getAvailable());
        assertEquals(15, inv.getTotalQuantity());
        assertEquals(15, result.getAvailable());
    }

    @Test
    void adjustForSale_decrementsAvailable() {
        StoreInventoryEntity inv = StoreInventoryEntity.builder()
                .id(1L).storeId(1L).productId(2L).available(10).sold(0).build();
        when(storeInventoryRepository.findByStoreIdAndProductId(1L, 2L)).thenReturn(Optional.of(inv));
        when(storeInventoryRepository.save(inv)).thenReturn(inv);
        when(storeInventoryEntityMapper.toDomain(inv))
                .thenReturn(StoreInventory.builder().id(1L).available(7).sold(3).build());

        service.adjustForSale(1L, 2L, 3, "ORDER", "o1", "sale");
        assertEquals(7, inv.getAvailable());
        assertEquals(3, inv.getSold());
    }

    @Test
    void adjustForSale_throwsWhenInsufficient() {
        StoreInventoryEntity inv = StoreInventoryEntity.builder()
                .id(1L).storeId(1L).productId(2L).available(1).build();
        when(storeInventoryRepository.findByStoreIdAndProductId(1L, 2L)).thenReturn(Optional.of(inv));

        assertThrows(IllegalArgumentException.class,
                () -> service.adjustForSale(1L, 2L, 5, "ORDER", "o1", "sale"));
    }

    @Test
    void adjustForSale_throwsWhenMissing() {
        when(storeInventoryRepository.findByStoreIdAndProductId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.adjustForSale(1L, 2L, 1, "ORDER", "o1", "sale"));
    }

    @Test
    void adjustBlocked_createsOrUpdates() {
        when(storeInventoryRepository.findByStoreIdAndProductId(1L, 2L)).thenReturn(Optional.empty());
        when(storeInventoryRepository.save(any(StoreInventoryEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storeInventoryEntityMapper.toDomain(any(StoreInventoryEntity.class)))
                .thenReturn(StoreInventory.builder().storeId(1L).productId(2L).blocked(2).build());

        StoreInventory result = service.adjustBlocked(1L, 2L, 2, "BLOCK");
        assertEquals(2, result.getBlocked());
        verify(inventoryTransactionRepository).save(any());
    }
}
