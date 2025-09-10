package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.mapper.BusinessDTOMapper;
import biz.craftline.server.feature.businessstore.api.request.AddNewBusinessRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
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
import static org.mockito.Mockito.when;

class BusinessEntityControllerTest {

    @Mock
    private BusinessEntityService businessEntityService;

    @Mock
    private BusinessDTOMapper mapper;

    @InjectMocks
    private BusinessEntityController businessEntityController;

    @BeforeEach
    void setUp() {
        try {
            MockitoAnnotations.openMocks(this).close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void addBusiness_Success() {
        // Arrange
        AddNewBusinessRequest request = AddNewBusinessRequest.builder()
                .businessName("Test Business")
                .description("Test Description")
                .build();

        Business business = Business.builder()
                .id(1L)
                .businessName("Test Business")
                .description("Test Description")
                .build();

        BusinessDTO expectedDto = BusinessDTO.builder()
                .id(1L)
                .businessName("Test Business")
                .description("Test Description")
                .build();

        when(mapper.toDomain(request))
                .thenReturn(business);
        when(businessEntityService.save(any(Business.class)))
                .thenReturn(business);
        when(mapper.toDTO(business))
                .thenReturn(expectedDto);

        // Act
        ResponseEntity<APIResponse<BusinessDTO>> response = businessEntityController.addBusiness(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(expectedDto, response.getBody().getData());
    }
}
