package biz.craftline.server.feature.customermanagement.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Customer API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    
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
    private int status;
    private int loyaltyPoints;
    private String preferredPayment;
    private int totalOrders;
    private double totalSpent;
    private LocalDateTime joinDate;
    private LocalDateTime lastOrderDate;
}

