package biz.craftline.server.feature.businesstype.api.request;

import lombok.Data;

@Data
public class AddCategoryRequest {
    private String name;
    private Long parentId; // optional
}
