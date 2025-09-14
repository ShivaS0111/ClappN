package biz.craftline.server.feature.ordermanagement.domain.service;

import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import java.util.List;

/**
 * Service interface for managing Order entities in the order management feature.
 */
public interface OrderService {
    List<Order> getAllOrders();
    Order getOrder(Long id);
    Order placeOrder(Order order);
    Order updateOrder(Long id, Order order);
    void deleteOrder(Long id);
    void cancelOrder(Long id);
    void completeOrder(Long id);
}
