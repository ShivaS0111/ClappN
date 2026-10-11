package biz.craftline.server.feature.ordermanagement.domain.model;

import biz.craftline.server.feature.paymentmanagement.domain.model.PaymentInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain model for Order.
 * Represents an order in the system.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Order {
    /** Order ID */
    private Long id;
    /** Customer ID */
    private Long storeId;

    private Double totalAmount;
    /** Customer ID */
    private Long customerId;
    /** Date the order was placed */
    private LocalDateTime orderDate;
    /** Status of the order */
    private String status;

    // Pricing breakdown
    private Double subtotal;
    private Double totalGst;
    private Double totalDiscount;
    private Double billDiscount;
    private String billDiscountType;
    private String couponCode;
    /** APPLY_SERVER_FULL | APPLY_CLIENT_CLAIMED — confirms how to resolve a discount mismatch. */
    private String discountResolution;
    /**
     * Skip acknowledge for proceedable lower client claim — apply client amounts directly.
     */
    private Boolean acceptLowerClientDiscount;
    private String notes;

    /** List of order items */
    private List<OrderItem> items;
    /** Delivery information */
    private DeliveryInfo deliveryInfo;
    /** Payment information */
    private PaymentInfo paymentInfo;}
