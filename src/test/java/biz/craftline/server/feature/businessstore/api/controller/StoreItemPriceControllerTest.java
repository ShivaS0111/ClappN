package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.UpdateStoreItemPriceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreItemPriceControllerTest {

    @Mock
    private StoreItemPriceService storeItemPriceService;

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
                .itemName("Delivery Service")
                .price(29.99)
                .status(1)
                .build();
        StoreItemPriceDTO expectedDto = StoreItemPriceDTO.builder()
                .id(1L)
                .serviceId(1L)
                .itemName("Delivery Service")
                .price(29.99)
                .status(1)
                .build();
        when(mapper.toDomain(any(UpdateStoreItemPriceRequest.class))).thenReturn(domainModel);
        when(storeItemPriceService.updateServicePrice(any(StoreItemPrice.class))).thenReturn(java.util.Optional.of(domainModel));
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
    void getAllPrices_Success() {
        // Arrange
        List<StoreItemPrice> domainModels = Arrays.asList(
                StoreItemPrice.builder()
                        .id(1L)
                        .serviceId(1L)
                        .itemName("Delivery Service")
                        .price(29.99)
                        .status(1)
                        .build(),
                StoreItemPrice.builder()
                        .id(2L)
                        .productLotId(2L)
                        .itemName("Product A")
                        .price(15.50)
                        .status(1)
                        .build()
        );
        List<StoreItemPriceDTO> expectedDtos = Arrays.asList(
                StoreItemPriceDTO.builder()
                        .id(1L)
                        .serviceId(1L)
                        .itemName("Delivery Service")
                        .price(29.99)
                        .status(1)
                        .build(),
                StoreItemPriceDTO.builder()
                        .id(2L)
                        .productLotId(2L)
                        .itemName("Product A")
                        .price(15.50)
                        .status(1)
                        .build()
        );
        when(storeItemPriceService.findAll()).thenReturn(domainModels);
        when(mapper.toDTO(domainModels.get(0))).thenReturn(expectedDtos.get(0));
        when(mapper.toDTO(domainModels.get(1))).thenReturn(expectedDtos.get(1));
        
        // Act
        ResponseEntity<APIResponse<List<StoreItemPriceDTO>>> response = storeItemPriceController.getAllPrices();
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getData().size());
        assertEquals(expectedDtos, response.getBody().getData());
    }

    @Test
    void getServicePrice_Success() {
        // Arrange
        Long serviceId = 1L;
        StoreItemPrice domainModel = StoreItemPrice.builder()
                .id(1L)
                .serviceId(serviceId)
                .itemName("Delivery Service")
                .price(29.99)
                .status(1)
                .build();
        StoreItemPriceDTO expectedDto = StoreItemPriceDTO.builder()
                .id(1L)
                .serviceId(serviceId)
                .itemName("Delivery Service")
                .price(29.99)
                .status(1)
                .build();
        when(storeItemPriceService.findByServiceId(serviceId)).thenReturn(java.util.Optional.of(domainModel));
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
