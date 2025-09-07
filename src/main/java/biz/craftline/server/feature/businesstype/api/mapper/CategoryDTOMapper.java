package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.CategoryDTO;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class CategoryDTOMapper {

    public  CategoryDTO toDTO(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .children(category.getChildren() != null
                        ? category.getChildren().stream()
                        .map(this::toDTO )
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    public  Category toDomain(CategoryDTO dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());

        if (dto.getChildren() != null) {
            category.setChildren(dto.getChildren()
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList()));
        }

        return category;
    }
}
