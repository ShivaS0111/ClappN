package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreItemPriceDTOMapper;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedServiceDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedServiceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class StoreOfferedServiceControllerTest {
    @Mock
    private ServicesOfferedByStoreService servicesOfferedByStoreService;
    @Mock
    private StoreOfferedServiceDTOMapper serviceMapper;
    @Mock
    private StoreItemPriceDTOMapper priceMapper;
    @Mock
    private StoreItemPriceService priceHandleService;
    private StoreOfferedServiceController storeOfferedServiceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeOfferedServiceController = new StoreOfferedServiceController(serviceMapper, priceMapper, servicesOfferedByStoreService, priceHandleService);
    }

    @Test
    void addStoreOfferedService_Success() {
        // Arrange
        AddNewStoreOfferedServiceRequest request = AddNewStoreOfferedServiceRequest.builder()
                .storeId(1L)
                .aliasName("Haircut")
                //.description("Professional haircut service")
                .build();

        StoreOfferedService service = StoreOfferedService.builder()
                .id(1L)
                .storeId(1L)
                .aliasName("Haircut")
                //.description("Professional haircut service")
                .build();

        when(servicesOfferedByStoreService.save(any(StoreOfferedService.class)))
                .thenReturn(service);

        // Act
        ResponseEntity<APIResponse<StoreOfferedServiceDTO>> response =
            storeOfferedServiceController.save(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Service added to store successfully", response.getBody().getMessage());
    }

    @Test
    void getStoreOfferedServices_Success() {
        // Arrange
        Long storeId = 1L;
        List<StoreOfferedService> services = Arrays.asList(
            StoreOfferedService.builder()
                .id(1L)
                .storeId(storeId)
                .aliasName("Haircut")
                //.description("Professional haircut")
                .build(),
            StoreOfferedService.builder()
                .id(2L)
                .storeId(storeId)
                .aliasName("Styling")
                //.description("Hair styling")
                .build()
        );

        when(servicesOfferedByStoreService.findServicesByStoreId(storeId))
                .thenReturn(Optional.of(services));

        // Act
        ResponseEntity<APIResponse<List<StoreOfferedServiceDTO>>> response =
            storeOfferedServiceController.list(storeId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Store services retrieved successfully", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());
    }
}
