package biz.craftline.server.feature.inventorymanagement.infra.entity;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "store_id")
    private Long storeId;

    private String lotCode;

    private int quantity;     // Total initially purchased
    private int blocked;      // Temporarily reserved
    private int sold;         // Permanently sold

    private double unitPrice;

    @Column(name = "currency_id")
    private Long currency;

    private boolean active = true;

    private Date purchasedAt;

    private Date mfgDate;

    private Date expiryAt;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    public int getAvailable() {
        return quantity - blocked - sold;
    }
}
