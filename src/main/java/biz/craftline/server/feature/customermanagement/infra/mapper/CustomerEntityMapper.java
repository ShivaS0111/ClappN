package biz.craftline.server.feature.customermanagement.infra.mapper;

import biz.craftline.server.feature.customermanagement.domain.model.Customer;
import biz.craftline.server.feature.customermanagement.infra.entity.CustomerEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Customer domain model and CustomerEntity.
 */
@Component
public class CustomerEntityMapper {
    
    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;
        
        return Customer.builder()
                .id(entity.getId())
                .storeId(entity.getStoreId())
                .businessId(entity.getBusinessId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .country(entity.getCountry())
                .birthday(entity.getBirthday())
                .status(entity.getStatus())
                .loyaltyPoints(entity.getLoyaltyPoints())
                .preferredPayment(entity.getPreferredPayment())
                .totalOrders(entity.getTotalOrders())
                .totalSpent(entity.getTotalSpent())
                .joinDate(entity.getJoinDate())
                .lastOrderDate(entity.getLastOrderDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public CustomerEntity toEntity(Customer domain) {
        if (domain == null) return null;
        
        return CustomerEntity.builder()
                .id(domain.getId())
                .storeId(domain.getStoreId())
                .businessId(domain.getBusinessId())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .address(domain.getAddress())
                .city(domain.getCity())
                .state(domain.getState())
                .zipCode(domain.getZipCode())
                .country(domain.getCountry())
                .birthday(domain.getBirthday())
                .status(domain.getStatus())
                .loyaltyPoints(domain.getLoyaltyPoints())
                .preferredPayment(domain.getPreferredPayment())
                .totalOrders(domain.getTotalOrders())
                .totalSpent(domain.getTotalSpent())
                .joinDate(domain.getJoinDate())
                .lastOrderDate(domain.getLastOrderDate())
                .build();
    }
}

