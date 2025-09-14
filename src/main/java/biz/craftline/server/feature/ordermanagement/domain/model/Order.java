package biz.craftline.server.feature.ordermanagement.domain.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Domain model for Order.
 * Represents an order in the system.
 */
@Data
public class Order {
    /** Order ID */
    private Long id;
    /** Customer ID */
    private Long customerId;
    /** Date the order was placed */
    private Date orderDate;
    /** Status of the order */
    private String status;
    /** List of order items */
    private List<OrderItem> items;
    /** Delivery information */
    private DeliveryInfo deliveryInfo;
    /** Payment information */
    private PaymentInfo paymentInfo;}

