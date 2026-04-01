package biz.craftline.server.feature.customermanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model for Customer.
 * Represents a customer in the system who can place orders at stores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    private Long id;
    private Long storeId;
    private Long businessId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private LocalDate birthday;
    private int status; // 0 = inactive, 1 = active, 2 = VIP
    private int loyaltyPoints;
    private String preferredPayment;
    private int totalOrders;
    private double totalSpent;
    private LocalDateTime joinDate;
    private LocalDateTime lastOrderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

