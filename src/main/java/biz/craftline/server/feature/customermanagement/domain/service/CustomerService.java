package biz.craftline.server.feature.customermanagement.domain.service;

import biz.craftline.server.feature.customermanagement.domain.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Customer management.
 */
public interface CustomerService {
    
    List<Customer> findAll();
    
    Optional<Customer> findById(Long id);
    
    List<Customer> findByStoreId(Long storeId);
    
    List<Customer> findByBusinessId(Long businessId);
    
    Optional<Customer> findByEmail(String email);
    
    Customer save(Customer customer);
    
    void deleteById(Long id);
    
    Customer updateLoyaltyPoints(Long customerId, int points);
    
    Customer recordOrder(Long customerId, double orderAmount);
}

