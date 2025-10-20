package biz.craftline.server.feature.businesstype.domain.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    @Builder.Default
    private List<Category> children = new ArrayList<>();
    private Integer status;
}
