package biz.craftline.server.feature.businessstore.api.request;

import lombok.Data;

@Data
public class AddNewStoreOfferedProductRequest {
    private Long storeId;
    private Long productId;
    private String productName;
    private String description;
    private String currency;
    private Double price;
    private int status;
    // Add other relevant fields as needed
}
