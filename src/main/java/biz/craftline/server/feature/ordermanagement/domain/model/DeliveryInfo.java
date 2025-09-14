package biz.craftline.server.feature.ordermanagement.domain.model;

/**
 * Domain model for DeliveryInfo.
 * Represents delivery details for an order.
 */
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
    private String deliveryDate;

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }
}
