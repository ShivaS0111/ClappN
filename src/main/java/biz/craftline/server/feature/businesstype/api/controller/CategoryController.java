package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.CategoryDTO;
import biz.craftline.server.feature.businesstype.api.mapper.CategoryDTOMapper;
import biz.craftline.server.feature.businesstype.api.request.AddCategoryRequest;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
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
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody AddCategoryRequest request) {
        return ResponseEntity.ok(
                categoryDTOMapper.toDTO(service.createCategory(request.getName(), request.getParentId()))
        );
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryDTO>> getCategoryTree() {
        return ResponseEntity.ok(
                service.getCategoryTree().stream()
                        .map(categoryDTOMapper::toDTO)
                        .toList()
        );
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<List<CategoryDTO>> getCategoryPath(@PathVariable Long id) {
        return ResponseEntity.ok(
                service.getCategoryPath(id).stream()
                        .map(categoryDTOMapper::toDTO)
                        .toList()
        );
    }
}
