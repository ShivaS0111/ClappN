package biz.craftline.server.feature.inventorymanagement.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreInventoryDTO {

    private Long id;

    private Long storeId;

    private Long productId;

    private int totalQuantity;
    private int available;
    private int blocked;
    private int sold;
    private int damaged;
}
