package biz.craftline.server.feature.ordermanagement.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Data Transfer Object for Order.
 * Encapsulates order data for API communication.
 */
@Setter
@Getter
public class OrderDTO {
    /** Order ID */
    private Long id;
    /** Customer ID */
    private Long customerId;
    /** Date the order was placed */
    private Date orderDate;
    /** Status of the order */
    private String status;
    /** List of order items */
    private List<OrderItemDTO> items;
    /** Delivery information */
    private DeliveryInfoDTO deliveryInfo;
    /** Payment information */
    private PaymentInfoDTO paymentInfo;

}

