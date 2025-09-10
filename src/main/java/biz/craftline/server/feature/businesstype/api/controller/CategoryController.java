package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.CategoryDTO;
import biz.craftline.server.feature.businesstype.api.mapper.CategoryDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddCategoryRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchCategoryRequest;
import biz.craftline.server.feature.businesstype.api.request.SearchRequest;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import biz.craftline.server.util.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @Autowired
    CategoryDTOMapper categoryDTOMapper;

    @PostMapping("/add")
    public ResponseEntity<APIResponse<CategoryDTO>> createCategory(@RequestBody AddCategoryRequest request) {
        return APIResponse.success(
                categoryDTOMapper.toDTO(service.createCategory(request.getName(), request.getParentId()))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<List<CategoryDTO>>> createCategory(@RequestBody SearchRequest request) {
        return APIResponse.success(
                service.searchCategory(request.keyword()).stream()
                        .map(categoryDTOMapper::toDTO)
                        .toList()
        );
    }

    @GetMapping("/tree")
    public ResponseEntity<APIResponse<List<CategoryDTO>>> getCategoryTree() {
        return APIResponse.success(
                service.getCategoryTree().stream()
                        .map(categoryDTOMapper::toDTO)
                        .toList()
        );
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<APIResponse<List<CategoryDTO>>> getCategoryPath(@PathVariable Long id) {
        return APIResponse.success(
                service.getCategoryPath(id).stream()
                        .map(categoryDTOMapper::toDTO)
                        .toList()
        );
    }
}
