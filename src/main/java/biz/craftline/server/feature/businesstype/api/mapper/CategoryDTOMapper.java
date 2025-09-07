package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.CategoryDTO;
import biz.craftline.server.feature.businesstype.domain.model.Category;

import java.util.stream.Collectors;

public class CategoryDTOMapper {

    public static CategoryDTO toDTO(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .children(category.getChildren() != null
                        ? category.getChildren().stream()
                        .map(CategoryDTOMapper::toDTO)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    public static Category toDomain(CategoryDTO dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());

        if (dto.getChildren() != null) {
            category.setChildren(dto.getChildren()
                    .stream()
                    .map(CategoryDTOMapper::toDomain)
                    .collect(Collectors.toList()));
        }

        return category;
    }
}
