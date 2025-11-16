package biz.craftline.server.feature.ordermanagement.api.dto;

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
    private Long itemType;
    /** Item ID (if applicable) */
    private Long itemId;
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

