package biz.craftline.server.feature.businessstore.api.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddNewStoreOfferedProductRequest {

    private String aliasName;

    private String description;
    private Long storeId;

    private int status;

    private Long businessProductId;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
