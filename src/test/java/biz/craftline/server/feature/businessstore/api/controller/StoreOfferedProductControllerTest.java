package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedProductDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedProductRequest;
import biz.craftline.server.feature.businessstore.domain.service.ProductsOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedProductDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreOfferedProductControllerTest {
    @Mock
    private StoreOfferedProductDTOMapper productMapper;
    @Mock
    private StoreItemPriceDTOMapper priceMapper;
    @Mock
    private ProductsOfferedByStoreService storeOfferedProductService;
    @Mock
    private StoreItemPriceService priceHandleService;

    @InjectMocks
    private StoreOfferedProductController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

  /*  @Test
    void testListProductsByStoreId() {
        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(1L);
        dto.setStoreId(1L);
        dto.setProductId(2L);
        dto.setProductName("Test Product");
        dto.setDescription("Test Description");
        dto.setCurrency("USD");
        dto.setPrice(100.0);

        when(storeOfferedProductService.findProductsByStoreId(1L)).thenReturn(Optional.of(Collections.emptyList()));
        when(productMapper.toDTO(any())).thenReturn(dto);

        ResponseEntity<APIResponse<List<StoreOfferedProductDTO>>> response = controller.list(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void testSaveProduct() {
        AddNewStoreOfferedProductRequest req = new AddNewStoreOfferedProductRequest();
        req.setStoreId(1L);
        req.setProductId(2L);
        req.setProductName("Test Product");
        req.setDescription("Test Description");
        req.setCurrency("USD");
        req.setPrice(100.0);

        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(1L);
        dto.setStoreId(1L);
        dto.setProductId(2L);
        dto.setProductName("Test Product");
        dto.setDescription("Test Description");
        dto.setCurrency("USD");
        dto.setPrice(100.0);

        when(productMapper.toDomain(req)).thenReturn(mock(biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct.class));
        when(storeOfferedProductService.save(any())).thenReturn(mock(biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct.class));
        when(productMapper.toDTO(any())).thenReturn(dto);

        ResponseEntity<APIResponse<StoreOfferedProductDTO>> response = controller.save(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Product added to store successfully", response.getBody().getMessage());
    }*/
}

