package biz.craftline.server.feature.businessstore.infra.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity(name = "product_lot_transaction")
public class ProductLotTransaction {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_lot_id")
    private ProductLotEntity productLot;


    @Column(name = "quantity_change")
    private int quantityChange;

    private String type; // BLOCK, UNBLOCK, SOLD, RETURN, ADJUST

    private String reason;

    @Column(name = "performed_by")
    private long performedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;
}
