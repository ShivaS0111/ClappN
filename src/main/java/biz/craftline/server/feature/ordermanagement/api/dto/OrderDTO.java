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
    /** Store ID */
    private Long storeId;
    /** Customer ID */
    private Long customerId;
    /** Total amount */
    private Double totalAmount;
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
    /**
     * After a discount mismatch (409), re-submit with one of:
     * {@code APPLY_SERVER_FULL} or {@code APPLY_CLIENT_CLAIMED} (only if proceedable).
     */
    private String discountResolution;
    /**
     * When true and client claimed a <b>lower</b> discount than entitled (store discretion),
     * place-order proceeds with the client claim immediately — no 409 / acknowledge step.
     * Ignored if the claim is higher than entitled (still requires reconfigure).
     */
    private Boolean acceptLowerClientDiscount;
    private String notes;

    /** List of order items */
    private List<OrderItemDTO> items;
    /** Delivery information */
    private DeliveryInfoDTO deliveryInfo;
    /** Payment information */
    private PaymentInfoDTO paymentInfo;
    /** Store name */
    private String storeName;
    /** Customer details */
    private biz.craftline.server.feature.customermanagement.api.dto.CustomerDTO customer;

}

