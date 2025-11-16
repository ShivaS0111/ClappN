package biz.craftline.server.feature.paymentmanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payment_info")
public class PaymentInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String paymentMethod;

    @Column
    private Double amount;

    @Column
    private String status;

    // Getters and setters
    // ...
}

