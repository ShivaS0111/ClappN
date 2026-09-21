package biz.craftline.server.feature.inventorymanagement.api.controller;

import biz.craftline.server.feature.inventorymanagement.api.dto.StoreInventoryDTO;
import biz.craftline.server.feature.inventorymanagement.api.mapper.StoreInventoryDTOMapper;
import biz.craftline.server.feature.inventorymanagement.domain.model.StoreInventory;
import biz.craftline.server.feature.inventorymanagement.domain.service.StoreInventoryService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreInventoryControllerTest {

    @Mock private StoreInventoryService storeInventoryService;
    @Mock private StoreInventoryDTOMapper storeInventoryDTOMapper;

    @InjectMocks
    private StoreInventoryController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getInventoryByStore() {
        StoreInventory inv = StoreInventory.builder().id(1L).storeId(1L).build();
        StoreInventoryDTO dto = StoreInventoryDTO.builder().id(1L).storeId(1L).build();
        when(storeInventoryService.findByStoreId(1L)).thenReturn(List.of(inv));
        when(storeInventoryDTOMapper.toDomain(inv)).thenReturn(dto);

        ResponseEntity<APIResponse<List<StoreInventoryDTO>>> response = controller.getInventoryByStore(1L);
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getLowStock() {
        StoreInventory inv = StoreInventory.builder().id(1L).available(2).build();
        StoreInventoryDTO dto = StoreInventoryDTO.builder().id(1L).available(2).build();
        when(storeInventoryService.findLowStockByStoreId(1L, 5)).thenReturn(List.of(inv));
        when(storeInventoryDTOMapper.toDomain(inv)).thenReturn(dto);

        assertEquals(1, controller.getLowStock(1L, 5).getBody().getData().size());
    }

    @Test
    void addStock() {
        StoreInventory inv = StoreInventory.builder().id(1L).available(10).build();
        StoreInventoryDTO dto = StoreInventoryDTO.builder().id(1L).available(10).build();
        when(storeInventoryService.addStock(1L, 2L, 5, "PURCHASE", "r1", "reason")).thenReturn(inv);
        when(storeInventoryDTOMapper.toDomain(inv)).thenReturn(dto);

        ResponseEntity<APIResponse<StoreInventoryDTO>> response =
                controller.addStock(1L, 2L, 5, "PURCHASE", "r1", "reason");
        assertEquals(10, response.getBody().getData().getAvailable());
    }

    @Test
    void sellStock() {
        StoreInventory inv = StoreInventory.builder().id(1L).available(7).build();
        StoreInventoryDTO dto = StoreInventoryDTO.builder().id(1L).available(7).build();
        when(storeInventoryService.adjustForSale(1L, 2L, 3, "ORDER", "o1", "sale")).thenReturn(inv);
        when(storeInventoryDTOMapper.toDomain(inv)).thenReturn(dto);

        ResponseEntity<APIResponse<StoreInventoryDTO>> response =
                controller.sellStock(1L, 2L, 3, "ORDER", "o1", "sale");
        assertEquals(7, response.getBody().getData().getAvailable());
    }
}
