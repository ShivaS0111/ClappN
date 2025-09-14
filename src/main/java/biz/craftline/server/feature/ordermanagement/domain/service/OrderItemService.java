package biz.craftline.server.feature.ordermanagement.domain.service;

import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import java.util.List;

public interface OrderItemService {
    List<OrderItem> getAllOrderItems();
    OrderItem getOrderItem(Long id);
    OrderItem addOrderItem(OrderItem orderItem);
    OrderItem updateOrderItem(Long id, OrderItem orderItem);
    void deleteOrderItem(Long id);
}

