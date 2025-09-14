package biz.craftline.server.feature.businessstore.api.dto;

import lombok.Data;

@Data
public class StoreOfferedProductDTO {
    private Long id;
    private Long storeId;
    private Long productId;
    private String productName;
    private String description;
    private String currency;
    private Double price;
    private int status;
    // Add other relevant fields as needed
}
