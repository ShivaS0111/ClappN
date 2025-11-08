package biz.craftline.server.feature.inventorymanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreInventory {

    private Long id;

    private Long storeId;

    private Long productId;

    private int totalQuantity;
    private int available;
    private int blocked;
    private int sold;
    private int damaged;

}
