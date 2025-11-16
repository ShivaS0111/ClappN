package biz.craftline.server.feature.paymentmanagement.domain.model;

/**
 * Domain model for PaymentInfo.
 * Represents payment details for an order.
 */
public class PaymentInfo {
    /** Payment method: CARD, CASH, ONLINE, WALLET */
    private String paymentMethod;
    /** Payment status: PENDING, PAID, FAILED, REFUNDED */
    private String paymentStatus;
    /** Transaction ID for payment */
    private String transactionId;
    /** Identifier for the payment info record */
    private Long id;
    /** Amount for the payment */
    private Double amount;
    /** Status of the payment info record */
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
