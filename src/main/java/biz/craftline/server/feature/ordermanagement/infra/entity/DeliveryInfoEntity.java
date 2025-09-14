package biz.craftline.server.feature.ordermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "delivery_info")
public class DeliveryInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String address;

    @Column
    private String deliveryDate;

    // Getters and setters
    // ...
}

