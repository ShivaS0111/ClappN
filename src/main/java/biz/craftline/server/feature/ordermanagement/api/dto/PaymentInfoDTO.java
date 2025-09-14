package biz.craftline.server.feature.ordermanagement.api.dto;

/**
 * Data Transfer Object for PaymentInfo.
 * Encapsulates payment details for an order.
 */
public class PaymentInfoDTO {
    /** Payment method: CARD, CASH, ONLINE, WALLET */
    private String paymentMethod;
    /** Payment status: PENDING, PAID, FAILED, REFUNDED */
    private String paymentStatus;
    /** Transaction ID for payment */
    private String transactionId;
    /** Payment ID */
    private Long id;
    /** Payment amount */
    private Double amount;
    /** Payment status */
    private String status;

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
