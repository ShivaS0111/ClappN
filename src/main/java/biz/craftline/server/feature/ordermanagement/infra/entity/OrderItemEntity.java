package biz.craftline.server.feature.ordermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column
    private Long productId;

    @Column
    private Long serviceId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "virtual_product_details_id")
    private VirtualProductDetailsEntity virtualProductDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "booking_details_id")
    private BookingDetailsEntity bookingDetails;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    // Getters and setters
    // ...
}

