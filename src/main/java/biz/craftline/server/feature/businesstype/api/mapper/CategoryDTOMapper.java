package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.CategoryDTO;
import biz.craftline.server.feature.businesstype.api.request.AddCategoryRequest;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class CategoryDTOMapper {

    public  CategoryDTO toDTO(Category category) {
        if (category == null) return null;

        //CategoryDTO parent = category.getParent() != null ? toParentDTO(category.getParent()): null;

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .status(category.getStatus())
                .parentId(category.getParentId())
                .children(category.getChildren() != null
                        ? category.getChildren().stream()
                        .map(this::toDTO )
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    private CategoryDTO toParentDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public  Category toDomain(CategoryDTO dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setStatus(dto.getStatus());
        category.setParentId(dto.getParentId());
        category.setDescription(dto.getDescription());

        if (dto.getChildren() != null) {
            category.setChildren(dto.getChildren()
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList()));
        }

        return category;
    }

    public  Category toDomain(AddCategoryRequest dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setName(dto.getName());
        category.setStatus(dto.getStatus());
        category.setDescription(dto.getDescription());
        category.setParentId(dto.getParentId());
        return category;
    }
}
