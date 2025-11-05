package biz.craftline.server.feature.businessstore.infra.entity;

import biz.craftline.server.util.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "product_lot_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLotTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_lot_id", nullable = false)
    private ProductLotEntity productLot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // BLOCK, UNBLOCK, SOLD, RETURN, ADJUST

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Column(name = "before_quantity", nullable = false)
    private int beforeQuantity;

    @Column(name = "after_quantity", nullable = false)
    private int afterQuantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "reason")
    private String reason;

    @Column(name = "reference_id")
    private String referenceId; // e.g. ORDER_ID, RESERVATION_ID

    @Column(name = "correlation_id")
    private String correlationId; // for idempotency / distributed tracing

    @Column(name = "performed_by")
    private long performedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Version
    private long version;
}
