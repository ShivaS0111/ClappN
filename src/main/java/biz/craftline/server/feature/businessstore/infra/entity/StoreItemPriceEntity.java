package biz.craftline.server.feature.businessstore.infra.entity;

import biz.craftline.server.feature.businessstore.application.enums.PriceType;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "store_item_price")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreItemPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "item_type")
    private Long itemType;  // e.g., product, productLot, service, packeage etc.

    @Column(name = "item_id")
    private Long itemId; // specific item id, it is one of the itemType reference id

    @Column(nullable = false)
    private Double price;

    private Long currency;

    // New: quantity threshold for bulk pricing
    @Column(name = "min_quantity")
    private Integer minQuantity = 1;

    // New: override type
    @Enumerated(EnumType.STRING)
    private PriceType overrideType = PriceType.DEFAULT;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    private int status;

    private long createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void validate() {
        if ((itemType == null || itemId != null)) {
            throw new IllegalStateException("itemType & itemId must set");
        }
    }
}
