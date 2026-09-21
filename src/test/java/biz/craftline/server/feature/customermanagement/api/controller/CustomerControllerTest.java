package biz.craftline.server.feature.customermanagement.api.controller;

import biz.craftline.server.feature.customermanagement.api.dto.CustomerDTO;
import biz.craftline.server.feature.customermanagement.api.mapper.CustomerDTOMapper;
import biz.craftline.server.feature.customermanagement.domain.model.Customer;
import biz.craftline.server.feature.customermanagement.domain.service.CustomerService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    @Mock private CustomerService customerService;
    @Mock private CustomerDTOMapper mapper;

    @InjectMocks
    private CustomerController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void list() {
        Customer customer = Customer.builder().id(1L).firstName("A").build();
        CustomerDTO dto = CustomerDTO.builder().id(1L).firstName("A").build();
        when(customerService.findAll()).thenReturn(List.of(customer));
        when(mapper.toDTO(customer)).thenReturn(dto);

        assertEquals(1, controller.list().getBody().getData().size());
    }

    @Test
    void getById_foundAndMissing() {
        Customer customer = Customer.builder().id(1L).build();
        CustomerDTO dto = CustomerDTO.builder().id(1L).build();
        when(customerService.findById(1L)).thenReturn(Optional.of(customer));
        when(customerService.findById(9L)).thenReturn(Optional.empty());
        when(mapper.toDTO(customer)).thenReturn(dto);

        assertEquals(HttpStatus.OK, controller.getById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getById(9L).getStatusCode());
    }

    @Test
    void listByStoreAndBusiness() {
        Customer customer = Customer.builder().id(1L).build();
        CustomerDTO dto = CustomerDTO.builder().id(1L).build();
        when(customerService.findByStoreId(1L)).thenReturn(List.of(customer));
        when(customerService.findByBusinessId(10L)).thenReturn(List.of(customer));
        when(mapper.toDTO(customer)).thenReturn(dto);

        assertEquals(1, controller.listByStoreId(1L).getBody().getData().size());
        assertEquals(1, controller.listByBusinessId(10L).getBody().getData().size());
    }

    @Test
    void createAndUpdate() {
        CustomerDTO dto = CustomerDTO.builder().firstName("A").build();
        Customer domain = Customer.builder().firstName("A").build();
        Customer saved = Customer.builder().id(1L).firstName("A").build();
        CustomerDTO savedDto = CustomerDTO.builder().id(1L).firstName("A").build();
        when(mapper.toDomain(dto)).thenReturn(domain);
        when(customerService.save(any())).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(savedDto);
        when(customerService.findById(1L)).thenReturn(Optional.of(saved));

        assertEquals(HttpStatus.CREATED, controller.create(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.update(1L, dto).getStatusCode());
        when(customerService.findById(9L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, controller.update(9L, dto).getStatusCode());
    }

    @Test
    void delete() {
        when(customerService.findById(1L)).thenReturn(Optional.of(Customer.builder().id(1L).build()));
        when(customerService.findById(9L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.OK, controller.delete(1L).getStatusCode());
        verify(customerService).deleteById(1L);
        assertEquals(HttpStatus.NOT_FOUND, controller.delete(9L).getStatusCode());
    }

    @Test
    void updateLoyaltyPoints() {
        Customer updated = Customer.builder().id(1L).loyaltyPoints(20).build();
        CustomerDTO dto = CustomerDTO.builder().id(1L).loyaltyPoints(20).build();
        when(customerService.updateLoyaltyPoints(1L, 5)).thenReturn(updated);
        when(mapper.toDTO(updated)).thenReturn(dto);
        when(customerService.updateLoyaltyPoints(9L, 5)).thenThrow(new RuntimeException("Customer not found"));

        assertEquals(20, controller.updateLoyaltyPoints(1L, 5).getBody().getData().getLoyaltyPoints());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateLoyaltyPoints(9L, 5).getStatusCode());
    }
}
