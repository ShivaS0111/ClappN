package biz.craftline.server.feature.ordermanagement.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for DeliveryInfo.
 * Encapsulates delivery details for an order.
 */
@Setter
@Getter
public class DeliveryInfoDTO {
    /** Type of delivery: STORE_PICKUP, HOME_DELIVERY */
    private String deliveryType;
    /** Delivery address */
    private String address;
    /** Delivery status */
    private String deliveryStatus;
    /** Tracking number for delivery */
    private String trackingNumber;
    /** Delivery date */
    private String deliveryDate;

}
