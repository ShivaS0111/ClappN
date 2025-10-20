package biz.craftline.server.feature.businesstype.api.request;

import lombok.Data;

@Data
public class AddCategoryRequest {
    private String name;
    private String description;
    private Long parentId;
    private int status;

    public AddCategoryRequest(String name,
                              String description,
                              Long parentId,
                              int status){
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.status = status;

    }
}
