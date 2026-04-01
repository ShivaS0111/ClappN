package biz.craftline.server.feature.customermanagement.api.mapper;

import biz.craftline.server.feature.customermanagement.api.dto.CustomerDTO;
import biz.craftline.server.feature.customermanagement.domain.model.Customer;
import org.springframework.stereotype.Component;

/**
 * Mapper between Customer domain model and CustomerDTO.
 */
@Component
public class CustomerDTOMapper {
    
    public CustomerDTO toDTO(Customer domain) {
        if (domain == null) return null;
        
        return CustomerDTO.builder()
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
    
    public Customer toDomain(CustomerDTO dto) {
        if (dto == null) return null;
        
        return Customer.builder()
                .id(dto.getId())
                .storeId(dto.getStoreId())
                .businessId(dto.getBusinessId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .birthday(dto.getBirthday())
                .status(dto.getStatus())
                .loyaltyPoints(dto.getLoyaltyPoints())
                .preferredPayment(dto.getPreferredPayment())
                .totalOrders(dto.getTotalOrders())
                .totalSpent(dto.getTotalSpent())
                .joinDate(dto.getJoinDate())
                .lastOrderDate(dto.getLastOrderDate())
                .build();
    }
}

