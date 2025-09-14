package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.OrderItemDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.OrderItemDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderItemService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {
    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping
    public List<OrderItemDTO> getAllOrderItems() {
        List<OrderItem> items = orderItemService.getAllOrderItems();
        List<OrderItemDTO> dtos = new ArrayList<>();
        for (OrderItem item : items) {
            dtos.add(OrderItemDTOMapper.toDTO(item));
        }
        return dtos;
    }

    @GetMapping("/{id}")
    public OrderItemDTO getOrderItem(@PathVariable Long id) {
        OrderItem item = orderItemService.getOrderItem(id);
        return item != null ? OrderItemDTOMapper.toDTO(item) : null;
    }

    @PostMapping
    public OrderItemDTO addOrderItem(@RequestBody OrderItemDTO dto) {
        OrderItem item = OrderItemDTOMapper.fromDTO(dto);
        OrderItem saved = orderItemService.addOrderItem(item);
        return OrderItemDTOMapper.toDTO(saved);
    }

    @PutMapping("/{id}")
    public OrderItemDTO updateOrderItem(@PathVariable Long id, @RequestBody OrderItemDTO dto) {
        OrderItem item = OrderItemDTOMapper.fromDTO(dto);
        OrderItem updated = orderItemService.updateOrderItem(id, item);
        return updated != null ? OrderItemDTOMapper.toDTO(updated) : null;
    }

    @DeleteMapping("/{id}")
    public void deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
    }
}

