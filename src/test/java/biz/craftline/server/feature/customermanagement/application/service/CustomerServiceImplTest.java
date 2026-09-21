package biz.craftline.server.feature.customermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.customermanagement.domain.model.Customer;
import biz.craftline.server.feature.customermanagement.infra.entity.CustomerEntity;
import biz.craftline.server.feature.customermanagement.infra.mapper.CustomerEntityMapper;
import biz.craftline.server.feature.customermanagement.infra.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @Mock private CustomerRepository repository;
    @Mock private CustomerEntityMapper mapper;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(securityContextService.getAccessibleStoreIds()).thenReturn(null);
        doNothing().when(securityContextService).validateStoreAccess(anyLong());
        doNothing().when(securityContextService).validateBusinessAccess(anyLong());
    }

    @Test
    void findAll_unrestricted() {
        CustomerEntity entity = CustomerEntity.builder().id(1L).firstName("A").build();
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(Customer.builder().id(1L).firstName("A").build());

        assertEquals(1, service.findAll().size());
    }

    @Test
    void findAll_scopedStores() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(1L));
        CustomerEntity entity = CustomerEntity.builder().id(1L).storeId(1L).firstName("A").build();
        when(repository.findByStoreIdIn(List.of(1L))).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(Customer.builder().id(1L).storeId(1L).build());

        assertEquals(1, service.findAll().size());
    }

    @Test
    void findAll_emptyAccessible_returnsEmpty() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of());
        assertTrue(service.findAll().isEmpty());
        verify(repository, never()).findAll();
    }

    @Test
    void findById_validatesStore() {
        CustomerEntity entity = CustomerEntity.builder().id(1L).storeId(1L).firstName("A").build();
        Customer domain = Customer.builder().id(1L).storeId(1L).firstName("A").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Customer> result = service.findById(1L);
        assertTrue(result.isPresent());
        verify(securityContextService).validateStoreAccess(1L);
    }

    @Test
    void findByStoreId() {
        when(repository.findByStoreId(1L)).thenReturn(List.of(CustomerEntity.builder().id(1L).build()));
        when(mapper.toDomain(any())).thenReturn(Customer.builder().id(1L).build());
        assertEquals(1, service.findByStoreId(1L).size());
    }

    @Test
    void findByBusinessId() {
        when(repository.findByBusinessId(10L)).thenReturn(List.of(CustomerEntity.builder().id(1L).build()));
        when(mapper.toDomain(any())).thenReturn(Customer.builder().id(1L).build());
        assertEquals(1, service.findByBusinessId(10L).size());
    }

    @Test
    void findByEmail_validatesBusinessWhenNoStore() {
        CustomerEntity entity = CustomerEntity.builder().id(1L).businessId(10L).email("a@b.com").firstName("A").build();
        Customer domain = Customer.builder().id(1L).businessId(10L).email("a@b.com").build();
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertTrue(service.findByEmail("a@b.com").isPresent());
        verify(securityContextService).validateBusinessAccess(10L);
    }

    @Test
    void findByEmail_noScope_nonAdmin_throws() {
        CustomerEntity entity = CustomerEntity.builder().id(1L).email("a@b.com").firstName("A").build();
        Customer domain = Customer.builder().id(1L).email("a@b.com").build();
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(securityContextService.isSystemAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.findByEmail("a@b.com"));
    }

    @Test
    void save_setsJoinDateAndPersists() {
        Customer customer = Customer.builder().storeId(1L).firstName("A").build();
        CustomerEntity entity = CustomerEntity.builder().storeId(1L).firstName("A").build();
        CustomerEntity saved = CustomerEntity.builder().id(1L).storeId(1L).firstName("A").build();
        when(mapper.toEntity(customer)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(Customer.builder().id(1L).storeId(1L).firstName("A").build());

        Customer result = service.save(customer);
        assertEquals(1L, result.getId());
        assertNotNull(entity.getJoinDate());
    }

    @Test
    void deleteById_validatesThenDeletes() {
        when(repository.findById(1L)).thenReturn(Optional.of(
                CustomerEntity.builder().id(1L).storeId(1L).firstName("A").build()));
        service.deleteById(1L);
        verify(securityContextService).validateStoreAccess(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void updateLoyaltyPoints() {
        CustomerEntity entity = CustomerEntity.builder().id(1L).storeId(1L).loyaltyPoints(5).firstName("A").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(Customer.builder().id(1L).loyaltyPoints(15).build());

        Customer result = service.updateLoyaltyPoints(1L, 10);
        assertEquals(15, entity.getLoyaltyPoints());
        assertEquals(15, result.getLoyaltyPoints());
    }

    @Test
    void updateLoyaltyPoints_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateLoyaltyPoints(1L, 1));
    }

    @Test
    void recordOrder_updatesTotals() {
        CustomerEntity entity = CustomerEntity.builder()
                .id(1L).storeId(1L).totalOrders(0).totalSpent(0).loyaltyPoints(0).firstName("A").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(Customer.builder().id(1L).totalOrders(1).totalSpent(25.5).build());

        service.recordOrder(1L, 25.5);
        assertEquals(1, entity.getTotalOrders());
        assertEquals(25.5, entity.getTotalSpent());
        assertNotNull(entity.getLastOrderDate());
    }
}
