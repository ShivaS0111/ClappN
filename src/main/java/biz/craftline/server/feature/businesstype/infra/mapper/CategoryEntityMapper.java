package biz.craftline.server.feature.businesstype.infra.mapper;

import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;

import java.util.stream.Collectors;

public class CategoryEntityMapper {

    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;

        Category category = new Category();
        category.setId(entity.getId());
        category.setName(entity.getName());
        category.setParent(toDomain(entity.getParent()));

        if (entity.getChildren() != null) {
            category.setChildren(entity.getChildren()
                    .stream()
                    .map(CategoryEntityMapper::toDomain)
                    .collect(Collectors.toList()));
        }

        return category;
    }

    public static CategoryEntity toEntity(Category domain) {
        if (domain == null) return null;

        CategoryEntity entity = new CategoryEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setParent(toEntity(domain.getParent()));

        if (domain.getChildren() != null) {
            entity.setChildren(domain.getChildren()
                    .stream()
                    .map(CategoryEntityMapper::toEntity)
                    .collect(Collectors.toList()));
        }

        return entity;
    }
}
