package biz.craftline.server.feature.ordermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Date orderDate;

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_info_id")
    private DeliveryInfoEntity deliveryInfo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_info_id")
    private PaymentInfoEntity paymentInfo;

    // Getters and setters
    // ...
}

