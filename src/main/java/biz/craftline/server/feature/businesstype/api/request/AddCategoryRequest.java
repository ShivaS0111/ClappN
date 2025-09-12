package biz.craftline.server.feature.businesstype.api.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AddCategoryRequest {
    private String name;
    private Long parentId; // optional

    public AddCategoryRequest(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }
}
