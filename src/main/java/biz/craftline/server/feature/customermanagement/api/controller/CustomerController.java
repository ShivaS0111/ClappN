package biz.craftline.server.feature.customermanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.customermanagement.api.dto.CustomerDTO;
import biz.craftline.server.feature.customermanagement.api.mapper.CustomerDTOMapper;
import biz.craftline.server.feature.customermanagement.domain.model.Customer;
import biz.craftline.server.feature.customermanagement.domain.service.CustomerService;
import biz.craftline.server.util.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing customers.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    private final CustomerDTOMapper mapper;
    
    /**
     * List all customers.
     */
    @Operation(summary = "List all customers", description = "Returns all customers.")
    @ApiResponse(responseCode = "200", description = "List of customers returned successfully.")
    @GetMapping
    @RequirePermission("customer.read")
    public ResponseEntity<APIResponse<List<CustomerDTO>>> list() {
        List<CustomerDTO> customers = customerService.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(customers, "Customers retrieved successfully");
    }
    
    /**
     * Get customer by ID.
     */
    @Operation(summary = "Get customer by ID", description = "Returns a customer by ID.")
    @ApiResponse(responseCode = "200", description = "Customer returned successfully.")
    @GetMapping("/{id}")
    @RequirePermission("customer.read")
    public ResponseEntity<APIResponse<CustomerDTO>> getById(@PathVariable Long id) {
        return customerService.findById(id)
                .map(c -> APIResponse.success(mapper.toDTO(c), "Customer retrieved successfully"))
                .orElse(APIResponse.error("Customer not found", HttpStatus.NOT_FOUND));
    }
    
    /**
     * List customers by store ID.
     */
    @Operation(summary = "List customers by store ID", description = "Returns all customers for a given store.")
    @ApiResponse(responseCode = "200", description = "List of customers returned successfully.")
    @GetMapping("/store/{storeId}")
    @RequirePermission("customer.read")
    public ResponseEntity<APIResponse<List<CustomerDTO>>> listByStoreId(@PathVariable Long storeId) {
        List<CustomerDTO> customers = customerService.findByStoreId(storeId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(customers, "Customers retrieved successfully");
    }
    
    /**
     * List customers by business ID.
     */
    @Operation(summary = "List customers by business ID", description = "Returns all customers for a given business.")
    @ApiResponse(responseCode = "200", description = "List of customers returned successfully.")
    @GetMapping("/business/{businessId}")
    @RequirePermission("customer.read")
    public ResponseEntity<APIResponse<List<CustomerDTO>>> listByBusinessId(@PathVariable Long businessId) {
        List<CustomerDTO> customers = customerService.findByBusinessId(businessId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(customers, "Customers retrieved successfully");
    }
    
    /**
     * Create a new customer.
     */
    @Operation(summary = "Create customer", description = "Creates a new customer.")
    @ApiResponse(responseCode = "201", description = "Customer created successfully.")
    @PostMapping
    @RequirePermission("customer.create")
    public ResponseEntity<APIResponse<CustomerDTO>> create(@RequestBody CustomerDTO dto) {
        Customer customer = mapper.toDomain(dto);
        Customer saved = customerService.save(customer);
        return APIResponse.success(mapper.toDTO(saved), "Customer created successfully", HttpStatus.CREATED);
    }
    
    /**
     * Update an existing customer.
     */
    @Operation(summary = "Update customer", description = "Updates an existing customer.")
    @ApiResponse(responseCode = "200", description = "Customer updated successfully.")
    @PutMapping("/{id}")
    @RequirePermission("customer.update")
    public ResponseEntity<APIResponse<CustomerDTO>> update(@PathVariable Long id, @RequestBody CustomerDTO dto) {
        return customerService.findById(id).map(existing -> {
            Customer customer = mapper.toDomain(dto);
            customer.setId(id);
            Customer saved = customerService.save(customer);
            return APIResponse.success(mapper.toDTO(saved), "Customer updated successfully");
        }).orElse(APIResponse.error("Customer not found", HttpStatus.NOT_FOUND));
    }
    
    /**
     * Delete a customer.
     */
    @Operation(summary = "Delete customer", description = "Deletes a customer by ID.")
    @ApiResponse(responseCode = "200", description = "Customer deleted successfully.")
    @DeleteMapping("/{id}")
    @RequirePermission("customer.delete")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable Long id) {
        if (customerService.findById(id).isEmpty()) {
            return APIResponse.error("Customer not found", HttpStatus.NOT_FOUND);
        }
        customerService.deleteById(id);
        return APIResponse.success("Customer deleted successfully");
    }
    
    /**
     * Update customer loyalty points.
     */
    @Operation(summary = "Update loyalty points", description = "Updates a customer's loyalty points.")
    @ApiResponse(responseCode = "200", description = "Loyalty points updated successfully.")
    @PostMapping("/{id}/loyalty-points")
    @RequirePermission("customer.update")
    public ResponseEntity<APIResponse<CustomerDTO>> updateLoyaltyPoints(
            @PathVariable Long id, 
            @RequestParam int points) {
        try {
            Customer updated = customerService.updateLoyaltyPoints(id, points);
            return APIResponse.success(mapper.toDTO(updated), "Loyalty points updated successfully");
        } catch (RuntimeException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}

