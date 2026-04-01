package biz.craftline.server.feature.customermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for Customer.
 */
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id")
    private Long storeId;
    
    @Column(name = "business_id")
    private Long businessId;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(unique = true)
    private String email;
    
    private String phone;
    
    private String address;
    
    private String city;
    
    private String state;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    private String country;
    
    private LocalDate birthday;
    
    @Column(nullable = false)
    private int status = 1; // 0 = inactive, 1 = active, 2 = VIP
    
    @Column(name = "loyalty_points")
    private int loyaltyPoints = 0;
    
    @Column(name = "preferred_payment")
    private String preferredPayment;
    
    @Column(name = "total_orders")
    private int totalOrders = 0;
    
    @Column(name = "total_spent")
    private double totalSpent = 0.0;
    
    @Column(name = "join_date")
    private LocalDateTime joinDate;
    
    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

