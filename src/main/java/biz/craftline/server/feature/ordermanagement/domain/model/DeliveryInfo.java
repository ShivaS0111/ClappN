package biz.craftline.server.feature.ordermanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model for DeliveryInfo.
 * Represents delivery details for an order.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryInfo {
    /** Type of delivery: STORE_PICKUP, HOME_DELIVERY */
    private String deliveryType;
    /** Delivery address */
    private String address;
    /** Delivery status */
    private String deliveryStatus;
    /** Tracking number for delivery */
    private String trackingNumber;
    /** Delivery date */
    private LocalDateTime deliveryDate;

    private LocalDateTime shippedDate;

    private String courierService;
}
