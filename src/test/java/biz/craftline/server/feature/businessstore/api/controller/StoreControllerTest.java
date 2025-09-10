package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreRequest;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
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

class StoreControllerTest {

    @Mock
    private StoreService storeService;

    @Mock
    private StoreDTOMapper mapper;

    @InjectMocks
    private StoreController storeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        ResponseEntity<APIResponse<StoreDTO>> response = storeController.addStore(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Store created successfully", response.getBody().getMessage());
        assertEquals(expectedDto, response.getBody().getData());
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
        ResponseEntity<APIResponse<List<StoreDTO>>> response = storeController.list(businessId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Stores retrieved successfully", response.getBody().getMessage());
        assertEquals(expectedDtos, response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());
    }
}
