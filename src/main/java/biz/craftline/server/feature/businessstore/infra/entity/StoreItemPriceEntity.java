package biz.craftline.server.feature.businessstore.infra.entity;

import biz.craftline.server.feature.businessstore.infra.config.enums.PriceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
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

    @ManyToOne
    @JoinColumn(name = "product_lot_id")
    private ProductLotEntity productLot;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private StoreOfferedServiceEntity service;

    @Column(nullable = false)
    private Double price;

    @ManyToOne
    @JoinColumn(name = "currency_id", nullable = false)
    private CurrencyEntity currency;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private CountryEntity country;

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
        if ((productLot == null && service == null) || (productLot != null && service != null)) {
            throw new IllegalStateException("Exactly one of productLot or service must be set.");
        }
    }
}
