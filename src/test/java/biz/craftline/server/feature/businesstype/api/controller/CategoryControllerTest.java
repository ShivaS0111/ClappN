package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.CategoryDTO;
import biz.craftline.server.feature.businesstype.api.mapper.CategoryDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddCategoryRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchRequest;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryControllerTest {
    @Mock
    private CategoryService service;
    @Mock
    private CategoryDTOMapper mapper;
    private CategoryController controller;

    public CategoryControllerTest() {
        MockitoAnnotations.openMocks(this);
        controller = new CategoryController(service, mapper);
    }

    @Test
    void testCreateCategory() {
        AddCategoryRequest req = new AddCategoryRequest("Shoes", null, 1L, 1);
        Category category = new Category();
        when(service.createCategory("Shoes", null)).thenReturn(category);
        CategoryDTO dto = new CategoryDTO();
        when(mapper.toDTO(category)).thenReturn(dto);
        ResponseEntity<APIResponse<CategoryDTO>> response = controller.createCategory(req);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testSearchCategory() {
        SearchRequest req = new SearchRequest("Shoes");
        Category category = new Category();
        when(service.searchCategory("Shoes")).thenReturn(List.of(category));
        CategoryDTO dto = new CategoryDTO();
        when(mapper.toDTO(category)).thenReturn(dto);
        ResponseEntity<APIResponse<List<CategoryDTO>>> response = controller.createCategory(req);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetCategoryTree() {
        Category category = new Category();
        when(service.getCategoryTree()).thenReturn(List.of(category));
        CategoryDTO dto = new CategoryDTO();
        when(mapper.toDTO(category)).thenReturn(dto);
        ResponseEntity<APIResponse<List<CategoryDTO>>> response = controller.getCategoryTree();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
