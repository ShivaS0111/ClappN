
package biz.craftline.server.feature.ordermanagement.domain.model;


import biz.craftline.server.feature.ordermanagement.api.dto.ReturnInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for OrderItem.
 * Represents a single item in an order.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    /** Item ID */
    private Long id;
    /** Type of item: PRODUCT, VIRTUAL_PRODUCT, SERVICE */
    private Long itemType;
    /** Product ID (if applicable) */
    private Long itemIId;
    /** Quantity of the item */
    private int quantity;
    /** Price of the item */
    private double price;
    /** Virtual product details (if applicable) */
    private VirtualProductDetails virtualProductDetails;
    /** Booking details (if applicable) */
    private BookingDetails bookingDetails;
    /** Delivery information for the item */
    private DeliveryInfo deliveryInfo;

    ReturnInfo returnInfo;
}