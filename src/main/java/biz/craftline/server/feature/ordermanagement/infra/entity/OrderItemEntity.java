package biz.craftline.server.feature.ordermanagement.infra.entity;

import biz.craftline.server.feature.ordermanagement.application.enums.OrderItemStatus;
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
    private String type;// PRODUCT / SERVICE / VIRTUAL

    @Column
    private Long productId;

    @Column
    private Long serviceId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus status;
    // e.g. PENDING, PACKED, SHIPPED, DELIVERED, RETURNED, CANCELLED

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_info_id")
    private DeliveryInfoEntity deliveryInfo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "return_info_id")
    private ReturnInfoEntity returnInfo;


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

