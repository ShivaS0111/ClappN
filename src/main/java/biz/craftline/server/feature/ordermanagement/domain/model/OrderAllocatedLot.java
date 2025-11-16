package biz.craftline.server.feature.ordermanagement.domain.model;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAllocatedLot {
    private Long orderItemId;
    private Long productLotId;
    private int allocatedQuantity;
}