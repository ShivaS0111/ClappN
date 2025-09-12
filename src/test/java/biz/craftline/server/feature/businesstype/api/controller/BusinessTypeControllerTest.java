package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BusinessTypeDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessTypeDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessTypeRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessTypeService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusinessTypeControllerTest {
    @Mock
    private BusinessTypeService service;
    @Mock
    private BusinessTypeDTOMapper mapper;
    @InjectMocks
    private BusinessTypeController controller;

    public BusinessTypeControllerTest() {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            // resources managed
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testList() {
        BusinessType type = new BusinessType();
        when(service.findAll()).thenReturn(List.of(type));
        BusinessTypeDTO dto = new BusinessTypeDTO();
        when(mapper.toDTO(type)).thenReturn(dto);
        ResponseEntity<APIResponse<List<BusinessTypeDTO>>> response = controller.list();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testSearch() {
        SearchRequest req = new SearchRequest("Retail");
        BusinessType type = new BusinessType();
        when(service.findByNameContaining("Retail")).thenReturn(List.of(type));
        BusinessTypeDTO dto = new BusinessTypeDTO();
        when(mapper.toDTO(type)).thenReturn(dto);
        ResponseEntity<APIResponse<List<BusinessTypeDTO>>> response = controller.search(req);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testAdd() {
        AddNewBusinessTypeRequest req = new AddNewBusinessTypeRequest("Retail", "Retail business");
        BusinessTypeDTO dto = BusinessTypeDTO.builder().businessName("Retail").description("Retail business").build();
        when(mapper.toDTO(any())).thenReturn(dto);
        ResponseEntity<APIResponse<BusinessTypeDTO>> response = controller.add(req);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
