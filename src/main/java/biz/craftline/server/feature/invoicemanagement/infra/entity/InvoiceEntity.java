package biz.craftline.server.feature.invoicemanagement.infra.entity;


import biz.craftline.server.feature.invoicemanagement.application.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    private BigDecimal totalAmount;

    @CreationTimestamp
    private LocalDateTime invoiceDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
}
