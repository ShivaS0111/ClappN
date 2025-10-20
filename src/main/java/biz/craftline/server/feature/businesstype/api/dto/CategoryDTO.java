package biz.craftline.server.feature.businesstype.api.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    //private CategoryDTO parent;
    private List<CategoryDTO> children;
    private Integer status;
}
