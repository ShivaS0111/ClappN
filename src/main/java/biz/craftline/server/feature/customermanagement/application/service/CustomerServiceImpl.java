package biz.craftline.server.feature.customermanagement.application.service;

import biz.craftline.server.feature.customermanagement.domain.model.Customer;
import biz.craftline.server.feature.customermanagement.domain.service.CustomerService;
import biz.craftline.server.feature.customermanagement.infra.entity.CustomerEntity;
import biz.craftline.server.feature.customermanagement.infra.mapper.CustomerEntityMapper;
import biz.craftline.server.feature.customermanagement.infra.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CustomerService.
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerRepository repository;
    private final CustomerEntityMapper mapper;
    
    @Override
    public List<Customer> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public List<Customer> findByStoreId(Long storeId) {
        return repository.findByStoreId(storeId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Customer> findByBusinessId(Long businessId) {
        return repository.findByBusinessId(businessId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Customer> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }
    
    @Override
    @Transactional
    public Customer save(Customer customer) {
        CustomerEntity entity = mapper.toEntity(customer);
        if (entity.getJoinDate() == null) {
            entity.setJoinDate(LocalDateTime.now());
        }
        CustomerEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    @Override
    @Transactional
    public Customer updateLoyaltyPoints(Long customerId, int points) {
        return repository.findById(customerId).map(entity -> {
            entity.setLoyaltyPoints(entity.getLoyaltyPoints() + points);
            return mapper.toDomain(repository.save(entity));
        }).orElseThrow(() -> new RuntimeException("Customer not found"));
    }
    
    @Override
    @Transactional
    public Customer recordOrder(Long customerId, double orderAmount) {
        return repository.findById(customerId).map(entity -> {
            entity.setTotalOrders(entity.getTotalOrders() + 1);
            entity.setTotalSpent(entity.getTotalSpent() + orderAmount);
            entity.setLastOrderDate(LocalDateTime.now());
            // Award loyalty points (1 point per dollar spent)
            entity.setLoyaltyPoints(entity.getLoyaltyPoints() + (int) orderAmount);
            return mapper.toDomain(repository.save(entity));
        }).orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}

