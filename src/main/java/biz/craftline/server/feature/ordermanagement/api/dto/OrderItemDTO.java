package biz.craftline.server.feature.ordermanagement.api.dto;

import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for OrderItem.
 * Represents a single item in an order for API communication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    /** Item ID */
    private Long id;
    /** Type of item: PRODUCT, VIRTUAL_PRODUCT, SERVICE */
    private String type;
    /** Product ID (if applicable) */
    private Long productId;
    /** Service ID (if applicable) */
    private Long serviceId;
    /** Quantity of the item */
    private int quantity;
    /** Price of the item */
    private double price;
    /** Virtual product details (if applicable) */
    private VirtualProductDetailsDTO virtualProductDetails;
    /** Booking details (if applicable) */
    private BookingDetailsDTO bookingDetails;
    /** Delivery information for the item */
    private DeliveryInfoDTO deliveryInfo;

    //ReturnInfoDTO returnInfo;
}

