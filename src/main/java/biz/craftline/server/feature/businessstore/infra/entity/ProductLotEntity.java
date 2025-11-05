package biz.craftline.server.feature.businessstore.infra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity( name = "product_lot" )
public class ProductLotEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private StoreOfferedProductEntity product;

    private String lotCode;

    private int quantity;     // Total initially purchased
    private int blocked;      // Temporarily reserved
    private int sold;         // Permanently sold

    private double unitPrice;

    @Column(name = "currency_id")
    private CurrencyEntity currency;

    @Column(name = "country_id")
    private Long country;

    private boolean active = true;

    @CreationTimestamp
    private Date purchasedAt;

    private Date expiryAt;

    @UpdateTimestamp
    private Date updatedAt;

    public int getAvailable() {
        return quantity - blocked - sold;
    }
}
