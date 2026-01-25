package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.OrderItemDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.OrderItemDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderItemService;
import biz.craftline.server.util.APIResponse;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<APIResponse<List<OrderItemDTO>>> getAllOrderItems() {
        List<OrderItem> items = orderItemService.getAllOrderItems();
        List<OrderItemDTO> dtos = new ArrayList<>();
        for (OrderItem item : items) {
            dtos.add(OrderItemDTOMapper.toDTO(item));
        }
        return APIResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<OrderItemDTO>> getOrderItem(@PathVariable Long id) {
        OrderItem item = orderItemService.getOrderItem(id);
        return APIResponse.ok(item != null ? OrderItemDTOMapper.toDTO(item) : null);
    }

    @PostMapping
    public ResponseEntity<APIResponse<OrderItemDTO>> addOrderItem(@RequestBody OrderItemDTO dto) {
        OrderItem item = OrderItemDTOMapper.fromDTO(dto);
        OrderItem saved = orderItemService.addOrderItem(item);
        return APIResponse.ok(OrderItemDTOMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<OrderItemDTO>> updateOrderItem(@PathVariable Long id, @RequestBody OrderItemDTO dto) {
        OrderItem item = OrderItemDTOMapper.fromDTO(dto);
        OrderItem updated = orderItemService.updateOrderItem(id, item);
        return APIResponse.ok(updated != null ? OrderItemDTOMapper.toDTO(updated) : null);
    }

    @DeleteMapping("/{id}")
    public void deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
    }
}

