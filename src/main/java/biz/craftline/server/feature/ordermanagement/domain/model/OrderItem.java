
package biz.craftline.server.feature.ordermanagement.domain.model;


/**
 * Domain model for OrderItem.
 * Represents a single item in an order.
 */
public class OrderItem {
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
    private VirtualProductDetails virtualProductDetails;
    /** Booking details (if applicable) */
    private BookingDetails bookingDetails;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public VirtualProductDetails getVirtualProductDetails() { return virtualProductDetails; }
    public void setVirtualProductDetails(VirtualProductDetails virtualProductDetails) { this.virtualProductDetails = virtualProductDetails; }
    public BookingDetails getBookingDetails() { return bookingDetails; }

    public void setBookingDetails(BookingDetails bookingDetails) { this.bookingDetails = bookingDetails; }
}