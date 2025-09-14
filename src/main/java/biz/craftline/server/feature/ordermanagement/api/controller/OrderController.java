package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.OrderDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.OrderDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * REST controller for managing orders in the order management feature.
 * Follows project coding standards and Spring conventions.
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
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderDTO> dtos = new ArrayList<>();
        for (Order order : orders) {
            dtos.add(OrderDTOMapper.toDTO(order));
        }
        return dtos;
    }

    /**
     * Returns an order by its ID.
     * @param id order ID
     * @return OrderDTO or null if not found
     */
    @GetMapping("/{id}")
    public OrderDTO getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return order != null ? OrderDTOMapper.toDTO(order) : null;
    }

    /**
     * Places a new order.
     * @param dto order DTO
     * @return placed OrderDTO
     */
    @PostMapping("/new")
    public OrderDTO placeOrder(@RequestBody OrderDTO dto) {
        // In a real app, convert DTO to domain model with mappers and lookups
        Order order = new Order();
        order.setCustomerId(dto.getCustomerId());
        order.setItems(new ArrayList<>()); // Populate from DTO
        order.setDeliveryInfo(null); // Populate from DTO
        order.setPaymentInfo(null); // Populate from DTO
        Order saved = orderService.placeOrder(order);
        return OrderDTOMapper.toDTO(saved);
    }

    /**
     * Updates an existing order.
     * @param id order ID
     * @param dto updated order DTO
     * @return updated OrderDTO or null if not found
     */
    @PutMapping("/update/{id}")
    public OrderDTO updateOrder(@PathVariable Long id, @RequestBody OrderDTO dto) {
        // In a real app, convert DTO to domain model
        Order updatedOrder = new Order();
        updatedOrder.setCustomerId(dto.getCustomerId());
        updatedOrder.setItems(new ArrayList<>()); // Populate from DTO
        updatedOrder.setDeliveryInfo(null); // Populate from DTO
        updatedOrder.setPaymentInfo(null); // Populate from DTO
        Order saved = orderService.updateOrder(id, updatedOrder);
        return saved != null ? OrderDTOMapper.toDTO(saved) : null;
    }

    /**
     * Cancels an order by its ID.
     * @param id order ID
     */
    @PostMapping("/{id}/cancel")
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }

    /**
     * Marks an order as completed by its ID.
     * @param id order ID
     */
    @PostMapping("/{id}/complete")
    public void completeOrder(@PathVariable Long id) {
        orderService.completeOrder(id);
    }
}

