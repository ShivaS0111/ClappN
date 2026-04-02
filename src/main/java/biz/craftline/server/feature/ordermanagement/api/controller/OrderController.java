package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.OrderDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.OrderDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import biz.craftline.server.util.APIResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * REST controller for managing orders in the order management feature.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * Constructor for dependency injection.
     * @param orderService the order service bean
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Returns all orders.
     * @return list of OrderDTO
     */
    @GetMapping
    public ResponseEntity<APIResponse<List<OrderDTO>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderDTO> dtos = orders.stream().map(OrderDTOMapper::toDTO).toList();
        return APIResponse.ok(dtos);
    }

    /**
     * Returns all orders for a specific store.
     * @param storeId store ID
     * @return list of OrderDTO
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<APIResponse<List<OrderDTO>>> getOrdersByStore(@PathVariable Long storeId) {
        List<Order> orders = orderService.getOrdersByStoreId(storeId);
        List<OrderDTO> dtos = orders.stream().map(OrderDTOMapper::toDTO).toList();
        return APIResponse.success(dtos, "Orders retrieved successfully");
    }

    /**
     * Returns all orders for a specific customer.
     * @param customerId customer ID
     * @return list of OrderDTO
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<APIResponse<List<OrderDTO>>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        List<OrderDTO> dtos = orders.stream().map(OrderDTOMapper::toDTO).toList();
        return APIResponse.success(dtos, "Orders retrieved successfully");
    }

    /**
     * Returns an order by its ID.
     * @param id order ID
     * @return OrderDTO or null if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<OrderDTO>> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        if (order != null) {
            return APIResponse.success(OrderDTOMapper.toDTO(order), "Order successfully retrieved");
        }
        return APIResponse.success(null, "Order not found");
    }

    /**
     * Places a new order. Fully maps items, delivery info, and payment info from the DTO.
     */
    @PostMapping("/new")
    public ResponseEntity<APIResponse<OrderDTO>> placeOrder(@Valid @RequestBody OrderDTO dto) {
        Order order = OrderDTOMapper.fromDTO(dto);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CREATED");
        Order saved = orderService.placeOrder(order);
        return APIResponse.success(OrderDTOMapper.toDTO(saved), "Order placed successfully");
    }

    /**
     * Updates an existing order. Fully maps all fields from the DTO.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<APIResponse<OrderDTO>> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderDTO dto) {
        Order updatedOrder = OrderDTOMapper.fromDTO(dto);
        Order saved = orderService.updateOrder(id, updatedOrder);
        if (saved != null) {
            return APIResponse.success(OrderDTOMapper.toDTO(saved), "Order updated successfully");
        }
        return APIResponse.success(null, "Order not found");
    }

    /**
     * Cancels an order by its ID.
     * @param id order ID
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<APIResponse<Void>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return APIResponse.success(null, "Order cancelled successfully");
    }

    /**
     * Marks an order as completed by its ID.
     * @param id order ID
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<APIResponse<Void>> completeOrder(@PathVariable Long id) {
        orderService.completeOrder(id);
        return APIResponse.success(null, "Order completed successfully");
    }

    /**
     * Updates the status of an order.
     * @param id order ID
     * @param status new status
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<APIResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id, @RequestParam String status) {
        Order order = orderService.getOrder(id);
        if (order == null) {
            return APIResponse.success(null, "Order not found");
        }
        order.setStatus(status);
        Order saved = orderService.updateOrder(id, order);
        return APIResponse.success(OrderDTOMapper.toDTO(saved), "Order status updated to " + status);
    }
}
