package biz.craftline.server.feature.ordermanagement.api.dto;

import biz.craftline.server.feature.paymentmanagement.api.dto.PaymentInfoDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Order.
 * Encapsulates order data for API communication.
 */
@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    /** Order ID */
    private Long id;
    /** Customer ID */
    private Long customerId;
    /** Date the order was placed */
    private LocalDateTime orderDate;
    /** Status of the order */
    private String status;
    /** List of order items */
    private List<OrderItemDTO> items;
    /** Delivery information */
    private DeliveryInfoDTO deliveryInfo;
    /** Payment information */
    private PaymentInfoDTO paymentInfo;

}

