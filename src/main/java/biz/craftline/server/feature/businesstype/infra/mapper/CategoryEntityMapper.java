package biz.craftline.server.feature.businesstype.infra.mapper;

import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class CategoryEntityMapper {

    public Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;

        Category category = new Category();
        category.setId(entity.getId());
        category.setName(entity.getName());
        category.setDescription(entity.getDescription());
        category.setStatus(entity.getStatus());
        if (entity.getParent() != null) {
            category.setParentId(entity.getParent().getId());
            Category parent = new Category();
            parent.setId(entity.getParent().getId());
            parent.setName(entity.getParent().getName());
        }

        // Children mapped shallow (avoid recursion back to parent)
        if (entity.getChildren() != null) {
            category.setChildren(entity.getChildren()
                    .stream()
                    .map(child -> {
                        Category c = new Category();
                        c.setId(child.getId());
                        c.setName(child.getName());
                        c.setStatus(child.getStatus());
                        return c;
                    })
                    .collect(Collectors.toList()));
        }

        return category;
    }

    public CategoryEntity toEntity(Category domain) {
        if (domain == null) return null;

        CategoryEntity entity = new CategoryEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());

        /*// Shallow parent
        if (domain.getParentId() != null) {
            CategoryEntity parent = new CategoryEntity();
            parent.setId(domain.getParent().getId());
            parent.setName(domain.getParent().getName());
            parent.setStatus( domain.getParent());
            entity.setParent(parent);
        }*/

        // Shallow children
        if (domain.getChildren() != null) {
            entity.setChildren(domain.getChildren()
                    .stream()
                    .map(child -> {
                        CategoryEntity e = new CategoryEntity();
                        e.setId(child.getId());
                        e.setName(child.getName());
                        e.setStatus(child.getStatus());
                        return e;
                    })
                    .collect(Collectors.toList()));
        }

        return entity;
    }
}
