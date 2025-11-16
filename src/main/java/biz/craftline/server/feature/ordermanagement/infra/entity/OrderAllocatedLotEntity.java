package biz.craftline.server.feature.ordermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "order_allocated_lot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAllocatedLotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "product_lot_id", nullable = false)
    private Long productLotId;

    private int allocatedQuantity;
}
