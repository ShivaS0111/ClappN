package biz.craftline.server.feature.inventorymanagement.api.controller;

import biz.craftline.server.feature.inventorymanagement.api.dto.ProductLotDTO;
import biz.craftline.server.feature.inventorymanagement.api.dto.ProductLotTransactionDTO;
import biz.craftline.server.feature.inventorymanagement.api.mapper.ProductLotDTOMapper;
import biz.craftline.server.feature.inventorymanagement.api.mapper.ProductLotTransactionDTOMapper;
import biz.craftline.server.feature.inventorymanagement.api.request.AddProductLotRequest;
import biz.craftline.server.feature.inventorymanagement.application.enums.TransactionType;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLot;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLotTransaction;
import biz.craftline.server.feature.inventorymanagement.domain.service.ProductLotService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductLotControllerTest {

    @Mock private ProductLotService lotService;
    @Mock private ProductLotDTOMapper productLotDTOMapper;
    @Mock private ProductLotTransactionDTOMapper lotTransactionDTOMapper;

    @InjectMocks
    private ProductLotController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createLot() {
        AddProductLotRequest req = new AddProductLotRequest();
        ProductLot domain = ProductLot.builder().id(1L).build();
        ProductLotDTO dto = new ProductLotDTO();
        when(productLotDTOMapper.toDomain(req)).thenReturn(domain);
        when(lotService.createLot(domain)).thenReturn(domain);
        when(productLotDTOMapper.toDTO(domain)).thenReturn(dto);

        ResponseEntity<APIResponse<ProductLotDTO>> response = controller.createLot(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getLot_andActiveLots() {
        ProductLot lot = ProductLot.builder().id(1L).build();
        ProductLotDTO dto = new ProductLotDTO();
        when(lotService.getLotById(1L)).thenReturn(lot);
        when(lotService.getAllActiveLots(10L)).thenReturn(List.of(lot));
        when(productLotDTOMapper.toDTO(lot)).thenReturn(dto);

        assertNotNull(controller.getLot(1L).getBody().getData());
        assertEquals(1, controller.getAllActiveLots(10L).getBody().getData().size());
    }

    @Test
    void updateAndDeleteLot() {
        ProductLotDTO dto = new ProductLotDTO();
        ProductLot domain = ProductLot.builder().id(1L).build();
        when(productLotDTOMapper.toDomain(dto)).thenReturn(domain);
        when(lotService.updateLot(domain)).thenReturn(domain);
        when(productLotDTOMapper.toDTO(domain)).thenReturn(dto);
        when(lotService.deleteLot(1L)).thenReturn(true);
        when(lotService.deleteLot(2L)).thenReturn(false);

        assertEquals(HttpStatus.OK, controller.updateLot(1L, dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.deleteLot(1L).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteLot(2L).getStatusCode());
    }

    @Test
    void recordAndListTransactions() {
        ProductLotTransaction tx = ProductLotTransaction.builder().id(1L).build();
        ProductLotTransactionDTO dto = new ProductLotTransactionDTO();
        when(lotService.recordTransaction(eq(1L), eq(TransactionType.BLOCK), eq(2), any(), any(), eq(9L)))
                .thenReturn(tx);
        when(lotService.getTransactionsForLot(1L)).thenReturn(List.of(tx));
        when(lotTransactionDTOMapper.toDTO(tx)).thenReturn(dto);

        assertNotNull(controller.recordTransaction(1L, TransactionType.BLOCK, 2, "r", "ref", 9L).getBody().getData());
        assertEquals(1, controller.getTransactionsForLot(1L).getBody().getData().size());
    }
}
