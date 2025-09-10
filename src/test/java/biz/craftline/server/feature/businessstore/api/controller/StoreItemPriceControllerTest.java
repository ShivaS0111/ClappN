package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.UpdateStoreItemPriceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreProductPriceService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreItemPriceControllerTest {

    @Mock
    private StoreProductPriceService storeProductPriceService;

    @Mock
    private StoreItemPriceDTOMapper mapper;

    @InjectMocks
    private StoreItemPriceController storeItemPriceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateServicePrice_Success() {
        // Arrange
        UpdateStoreItemPriceRequest request = UpdateStoreItemPriceRequest.builder()
                .serviceId(1L)
                .price(29.99)
                .build();
        StoreItemPrice domainModel = StoreItemPrice.builder()
                .id(1L)
                .serviceId(1L)
                .price(29.99)
                .build();
        StoreItemPriceDTO expectedDto = StoreItemPriceDTO.builder()
                .id(1L)
                .serviceId(1L)
                .price(29.99)
                .build();
        when(mapper.toDomain(any(UpdateStoreItemPriceRequest.class))).thenReturn(domainModel);
        when(storeProductPriceService.updateServicePrice(any(StoreItemPrice.class))).thenReturn(java.util.Optional.of(domainModel));
        when(mapper.toDTO(any(StoreItemPrice.class))).thenReturn(expectedDto);
        // Act
        ResponseEntity<APIResponse<StoreItemPriceDTO>> response = storeItemPriceController.updatePrice(request);
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedDto, response.getBody().getData());
    }

    @Test
    void getServicePrice_Success() {
        // Arrange
        Long serviceId = 1L;
        StoreItemPrice domainModel = StoreItemPrice.builder()
                .id(1L)
                .serviceId(serviceId)
                .price(29.99)
                .build();
        StoreItemPriceDTO expectedDto = StoreItemPriceDTO.builder()
                .id(1L)
                .serviceId(serviceId)
                .price(29.99)
                .build();
        when(storeProductPriceService.findByServiceId(serviceId)).thenReturn(java.util.Optional.of(domainModel));
        when(mapper.toDTO(domainModel)).thenReturn(expectedDto);
        // Act
        ResponseEntity<APIResponse<StoreItemPriceDTO>> response = storeItemPriceController.getServicePrice(serviceId);
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedDto, response.getBody().getData());
    }
}
