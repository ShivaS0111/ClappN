package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.CategoryEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryJpaRepository repository;
    private final CategoryEntityMapper categoryEntityMapper;

    @Override
    public Category createCategory(String name, Long parentId) {
        CategoryEntity parent = null;
        if (parentId != null) {
            parent = repository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
        }
        CategoryEntity entity = CategoryEntity.builder()
                .name(name)
                .parent(parent)
                .build();
        return categoryEntityMapper.toDomain(repository.save(entity));
    }

    @Override
    public List<Category> searchCategory(String keyword) {
        return repository.searchByKeyword(keyword).stream()
                .map(categoryEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> getCategoryTree() {
        return repository.findByParentIsNull().stream()
                .map(categoryEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> getCategoryPath(Long categoryId) {
        CategoryEntity current = repository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Category> path = new ArrayList<>();
        while (current != null) {
            path.add(0, categoryEntityMapper.toDomain(current));
            current = current.getParent();
        }
        return path;
    }

    @Override
    public List<Category> findAllByIds(List<Long> categories) {
        return repository.findAllById( categories ).stream().map( categoryEntityMapper::toDomain).toList();
    }
}
