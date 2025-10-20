package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BusinessTypeDTO;
import biz.craftline.server.feature.businesstype.api.dto.CategoryDTO;
import biz.craftline.server.feature.businesstype.api.mapper.CategoryDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.*;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import biz.craftline.server.util.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;
    private final CategoryDTOMapper categoryDTOMapper;

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<CategoryDTO>>> list() {
        return APIResponse.success(
                service.findAll().stream()
                        .map(categoryDTOMapper::toDTO)
                        .toList()
        );
    }

    @PostMapping("/add")
    public ResponseEntity<APIResponse<CategoryDTO>> createCategory(@RequestBody AddCategoryRequest request) {
        return APIResponse.success(
                categoryDTOMapper.toDTO(service.createCategory(request.getName(), request.getParentId()))
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<APIResponse<CategoryDTO>> update(@PathVariable("id") Long id,
                                                               @RequestBody AddCategoryRequest request) {

        Category categoryInput = categoryDTOMapper.toDomain(request);
        Category category = service.update(id,  categoryInput);
        return APIResponse.success(
                categoryDTOMapper.toDTO(category)
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
