package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BusinessProductDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessProductDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessProductRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchServiceByBusinessRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.service.BusinessProductsService;
import biz.craftline.server.feature.businesstype.domain.service.BusinessTypeService;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessProductControllerTest {

    @Mock private BusinessProductDTOMapper mapper;
    @Mock private BusinessProductsService service;
    @Mock private BusinessTypeService businessTypeService;
    @Mock private CategoryService categoryService;
    @InjectMocks private BusinessProductController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getById_success() {
        BusinessProduct product = BusinessProduct.builder().id(1L).name("P").build();
        when(service.findById(1L)).thenReturn(Optional.of(product));
        when(mapper.toDTO(product)).thenReturn(BusinessProductDTO.builder().id(1L).name("P").build());

        ResponseEntity<APIResponse<BusinessProductDTO>> response = controller.getById(1L);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void getById_missing_throws() {
        when(service.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> controller.getById(1L));
    }

    @Test
    void list_success() {
        when(service.findAll()).thenReturn(List.of(BusinessProduct.builder().id(1L).build()));
        when(mapper.toDTO(any())).thenReturn(new BusinessProductDTO());
        assertEquals(1, controller.list().getBody().getData().size());
    }

    @Test
    void listByBusinessType() {
        when(service.findByBusinessTypeId(5L)).thenReturn(List.of());
        assertTrue(controller.listByBusinessType(5L).getBody().getData().isEmpty());
    }

    @Test
    void search() {
        when(service.findBySearch("x")).thenReturn(List.of(BusinessProduct.builder().id(1L).build()));
        when(mapper.toDTO(any())).thenReturn(new BusinessProductDTO());
        assertEquals(1, controller.search(new SearchRequest("x")).getBody().getData().size());
    }

    @Test
    void searchByBusinessType() {
        when(service.findByBusinessTypeIdAndSearch(2L, "k")).thenReturn(List.of());
        assertTrue(controller.searchServiceByBusinessType(
                new SearchServiceByBusinessRequest("k", 2L)).getBody().getData().isEmpty());
    }

    @Test
    void add_success() {
        AddNewBusinessProductRequest req = mock(AddNewBusinessProductRequest.class);
        BusinessProduct domain = BusinessProduct.builder().name("P").build();
        when(mapper.toDomain(req)).thenReturn(domain);
        when(service.save(domain)).thenReturn(BusinessProduct.builder().id(9L).build());
        when(mapper.toDTO(any())).thenReturn(BusinessProductDTO.builder().id(9L).build());

        assertEquals(9L, controller.add(req).getBody().getData().getId());
    }

    @Test
    void update_success() {
        AddNewBusinessProductRequest req = mock(AddNewBusinessProductRequest.class);
        BusinessProduct domain = BusinessProduct.builder().name("P").build();
        when(mapper.toDomain(req)).thenReturn(domain);
        when(service.update(any())).thenReturn(BusinessProduct.builder().id(3L).build());
        when(mapper.toDTO(any())).thenReturn(BusinessProductDTO.builder().id(3L).build());

        assertEquals(3L, controller.update(3L, req).getBody().getData().getId());
    }

    @Test
    void delete_success() {
        doNothing().when(service).deleteProductById(1L);
        assertEquals("Deleted successfully", controller.delete(1L).getBody().getData());
    }

    @Test
    void addAll_emptyRequest() {
        assertTrue(controller.addAll(List.of()).getBody().getData().isEmpty());
        assertTrue(controller.addAll(null).getBody().getData().isEmpty());
    }

    @Test
    void addAll_withCategories() {
        AddNewBusinessProductRequest req = mock(AddNewBusinessProductRequest.class);
        when(req.getCategories()).thenReturn(List.of(4L));
        when(categoryService.findAllByIds(List.of(4L))).thenReturn(List.of(
                biz.craftline.server.feature.businesstype.domain.model.Category.builder().id(4L).build()));
        when(mapper.toDomain(req)).thenReturn(BusinessProduct.builder().name("P").build());
        when(service.save(anyList())).thenReturn(List.of(BusinessProduct.builder().id(1L).build()));
        when(mapper.toDTO(any())).thenReturn(BusinessProductDTO.builder().id(1L).build());

        assertEquals(1, controller.addAll(List.of(req)).getBody().getData().size());
    }

    @Test
    void addAll1_fromDtoList() {
        BusinessProductDTO dto = BusinessProductDTO.builder().name("P").build();
        when(mapper.toDomain(dto)).thenReturn(BusinessProduct.builder().name("P").build());
        when(service.save(anyList())).thenReturn(List.of(BusinessProduct.builder().id(2L).build()));
        when(mapper.toDTO(any())).thenReturn(BusinessProductDTO.builder().id(2L).build());

        assertEquals(1, controller.add(List.of(dto)).getBody().getData().size());
    }
}
