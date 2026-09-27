package biz.craftline.server.feature.customermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
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
    private final SecurityContextService securityContextService;
    
    @Override
    public List<Customer> findAll() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            // SYSTEM_ADMIN — unrestricted
            return repository.findAll().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        // Business-scoped users may have empty store list but accessible businesses
        List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();
        boolean hasStores = accessibleStoreIds != null && !accessibleStoreIds.isEmpty();
        boolean hasBusinesses = accessibleBusinessIds != null && !accessibleBusinessIds.isEmpty();
        if (!hasStores && !hasBusinesses) {
            return List.of();
        }
        if (hasStores && hasBusinesses) {
            java.util.LinkedHashMap<Long, Customer> byId = new java.util.LinkedHashMap<>();
            repository.findByStoreIdIn(accessibleStoreIds).stream()
                    .map(mapper::toDomain)
                    .forEach(c -> byId.put(c.getId(), c));
            repository.findByBusinessIdIn(accessibleBusinessIds).stream()
                    .map(mapper::toDomain)
                    .forEach(c -> byId.putIfAbsent(c.getId(), c));
            return List.copyOf(byId.values());
        }
        if (hasStores) {
            return repository.findByStoreIdIn(accessibleStoreIds).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        return repository.findByBusinessIdIn(accessibleBusinessIds).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Customer> findById(Long id) {
        Optional<Customer> customer = repository.findById(id).map(mapper::toDomain);
        customer.ifPresent(c -> {
            if (c.getStoreId() != null) {
                securityContextService.validateStoreAccess(c.getStoreId());
            } else if (c.getBusinessId() != null) {
                securityContextService.validateBusinessAccess(c.getBusinessId());
            }
        });
        return customer;
    }
    
    @Override
    public List<Customer> findByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        return repository.findByStoreId(storeId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Customer> findByBusinessId(Long businessId) {
        securityContextService.validateBusinessAccess(businessId);
        return repository.findByBusinessId(businessId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Customer> findByEmail(String email) {
        Optional<Customer> customer = repository.findByEmail(email).map(mapper::toDomain);
        customer.ifPresent(c -> {
            if (c.getStoreId() != null) {
                securityContextService.validateStoreAccess(c.getStoreId());
            } else if (c.getBusinessId() != null) {
                securityContextService.validateBusinessAccess(c.getBusinessId());
            } else if (!securityContextService.isSystemAdmin()) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Customer has no store/business scope");
            }
        });
        return customer;
    }
    
    @Override
    @Transactional
    public Customer save(Customer customer) {
        if (customer.getStoreId() == null && customer.getBusinessId() == null) {
            throw new IllegalArgumentException("storeId or businessId is required");
        }
        if (customer.getStoreId() != null) {
            securityContextService.validateStoreAccess(customer.getStoreId());
        }
        if (customer.getBusinessId() != null) {
            securityContextService.validateBusinessAccess(customer.getBusinessId());
        }
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
        CustomerEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        if (entity.getStoreId() != null) {
            securityContextService.validateStoreAccess(entity.getStoreId());
        } else if (entity.getBusinessId() != null) {
            securityContextService.validateBusinessAccess(entity.getBusinessId());
        } else if (!securityContextService.isSystemAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Customer has no store/business scope");
        }
        repository.deleteById(id);
    }
    
    @Override
    @Transactional
    public Customer updateLoyaltyPoints(Long customerId, int points) {
        return repository.findById(customerId).map(entity -> {
            if (entity.getStoreId() != null) {
                securityContextService.validateStoreAccess(entity.getStoreId());
            }
            entity.setLoyaltyPoints(entity.getLoyaltyPoints() + points);
            return mapper.toDomain(repository.save(entity));
        }).orElseThrow(() -> new RuntimeException("Customer not found"));
    }
    
    @Override
    @Transactional
    public Customer recordOrder(Long customerId, double orderAmount) {
        return repository.findById(customerId).map(entity -> {
            if (entity.getStoreId() != null) {
                securityContextService.validateStoreAccess(entity.getStoreId());
            } else if (entity.getBusinessId() != null) {
                securityContextService.validateBusinessAccess(entity.getBusinessId());
            } else if (!securityContextService.isSystemAdmin()) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Customer has no store/business scope");
            }
            entity.setTotalOrders(entity.getTotalOrders() + 1);
            entity.setTotalSpent(entity.getTotalSpent() + orderAmount);
            entity.setLastOrderDate(LocalDateTime.now());
            // Award loyalty points (1 point per dollar spent)
            entity.setLoyaltyPoints(entity.getLoyaltyPoints() + (int) orderAmount);
            return mapper.toDomain(repository.save(entity));
        }).orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}

