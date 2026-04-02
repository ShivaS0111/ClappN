package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.ordermanagement.api.dto.OrderItemDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.OrderItemDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderItemService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;
    private final OrderItemDTOMapper orderItemDTOMapper;


    @GetMapping
    @RequirePermission("order.read")
    public ResponseEntity<APIResponse<List<OrderItemDTO>>> getAllOrderItems() {
        List<OrderItem> items = orderItemService.getAllOrderItems();
        List<OrderItemDTO> dtos = new ArrayList<>();
        for (OrderItem item : items) {
            dtos.add(orderItemDTOMapper.toDTO(item));
        }
        return APIResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    @RequirePermission("order.read")
    public ResponseEntity<APIResponse<OrderItemDTO>> getOrderItem(@PathVariable Long id) {
        OrderItem item = orderItemService.getOrderItem(id);
        if(item!=null){
            return APIResponse.success(orderItemDTOMapper.toDTO(item), "Order item successfully retrieved ");
        } else{
            return APIResponse.success(null, "Order item not found");
        }
    }

    @PostMapping
    @RequirePermission("order.create")
    public ResponseEntity<APIResponse<OrderItemDTO>> addOrderItem(@RequestBody OrderItemDTO dto) {
        OrderItem item = orderItemDTOMapper.fromDTO(dto);
        OrderItem saved = orderItemService.addOrderItem(item);
        return APIResponse.ok(orderItemDTOMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @RequirePermission("order.update")
    public ResponseEntity<APIResponse<OrderItemDTO>> updateOrderItem(@PathVariable Long id, @RequestBody OrderItemDTO dto) {
        OrderItem item = orderItemDTOMapper.fromDTO(dto);
        OrderItem updated = orderItemService.updateOrderItem(id, item);
        return APIResponse.ok(updated != null ? orderItemDTOMapper.toDTO(updated) : null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("order.delete")
    public void deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
    }
}
