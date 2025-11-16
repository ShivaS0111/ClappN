package biz.craftline.server.feature.paymentmanagement.infra.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String gateway; // STRIPE / RAZORPAY
    private String providerPaymentId; // provider payment id (if available)
    private String providerOrderId; // provider's order/session id
    private String currency;
    private Long amount; // minor units
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String callbackUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}