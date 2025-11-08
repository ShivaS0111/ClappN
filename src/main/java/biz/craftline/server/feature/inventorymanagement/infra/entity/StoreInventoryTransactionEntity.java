package biz.craftline.server.feature.inventorymanagement.infra.entity;

import biz.craftline.server.feature.inventorymanagement.application.enums.InventoryAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;


@Entity
@Table(name = "store_inventory_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInventoryTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_inventory_id")
    private StoreInventoryEntity inventory;

    @Enumerated(EnumType.STRING)
    private InventoryAction action; // IN, OUT, TRANSFER, ADJUST, DAMAGE, EXPIRE

    private int quantityChange;

    private String referenceType; // e.g. ORDER, PURCHASE, RETURN

    private String referenceId;

    private String reason;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
