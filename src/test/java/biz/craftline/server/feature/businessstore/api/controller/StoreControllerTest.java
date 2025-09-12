package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreRequest;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreControllerTest {

    @Mock
    private StoreService storeService;

    @Mock
    private StoreDTOMapper mapper;

    @Mock
    private BusinessEntityService businessService;

    private StoreController storeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeController = new StoreController(mapper, storeService, businessService);
    }

    @Test
    void addStore_Success() {
        // Arrange
        AddNewStoreRequest request = AddNewStoreRequest.builder()
                .storeName("Test Store")
                .businessId(1L)
                .description("Test Description")
                .build();

        Store store = Store.builder()
                .id(1L)
                .storeName("Test Store")
                .description("Test Description")
                .status(1)
                .build();

        StoreDTO expectedDto = StoreDTO.builder()
                .id(1L)
                .storeName("Test Store")
                .description("Test Description")
                .status(1)
                .build();

        when(mapper.toDomain(any(AddNewStoreRequest.class))).thenReturn(store);
        when(storeService.save(any(Store.class))).thenReturn(store);
        when(mapper.toDTO(any(Store.class))).thenReturn(expectedDto);

        // Act
        APIResponse<StoreDTO> response = storeController.addStore(request).getBody();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Store created successfully", response.getMessage());
        assertEquals(expectedDto, response.getData());
    }

    @Test
    void listStoresByBusinessId_Success() {
        // Arrange
        long businessId = 1L;
        List<Store> stores = Arrays.asList(
            Store.builder().id(1L).storeName("Store 1").description("Desc 1").status(1).build(),
            Store.builder().id(2L).storeName("Store 2").description("Desc 2").status(1).build()
        );
        List<StoreDTO> expectedDtos = Arrays.asList(
            StoreDTO.builder().id(1L).storeName("Store 1").description("Desc 1").status(1).build(),
            StoreDTO.builder().id(2L).storeName("Store 2").description("Desc 2").status(1).build()
        );
        when(storeService.findStoresByBusiness(businessId)).thenReturn(stores);
        when(mapper.toDTO(stores.get(0))).thenReturn(expectedDtos.get(0));
        when(mapper.toDTO(stores.get(1))).thenReturn(expectedDtos.get(1));

        // Act
        APIResponse<List<StoreDTO>> response = storeController.list(businessId).getBody();

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Stores retrieved successfully", response.getMessage());
        assertEquals(expectedDtos, response.getData());
        assertEquals(2, response.getData().size());
    }
}
