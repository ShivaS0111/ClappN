package biz.craftline.server.feature.ordermanagement;

import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Test
    void testCreateAndReadOrder() {
        Order order = new Order();
        order.setCustomerId(123L);
        order.setOrderDate(new Date());
        order.setStatus("Order Created");
        Order saved = orderService.placeOrder(order);
        assertNotNull(saved.getId());
        Order fetched = orderService.getOrder(saved.getId());
        assertEquals(123L, fetched.getCustomerId());
    }

    @Test
    void testUpdateOrder() {
        Order order = new Order();
        order.setCustomerId(456L);
        order.setOrderDate(new Date());
        order.setStatus("Order Created");
        Order saved = orderService.placeOrder(order);
        saved.setCustomerId(789L);
        saved.setOrderDate(new Date());
        Order updated = orderService.updateOrder(saved.getId(), saved);
        assertEquals(789L, updated.getCustomerId());
    }

    @Test
    void testDeleteOrder() {
        Order order = new Order();
        order.setCustomerId(111L);
        order.setOrderDate(new Date());
        order.setStatus("Order Created");
        Order saved = orderService.placeOrder(order);
        orderService.deleteOrder(saved.getId());
        assertNull(orderService.getOrder(saved.getId()));
    }

    @Test
    void testGetAllOrders() {
        Order order1 = new Order();
        order1.setCustomerId(1L);
        order1.setOrderDate(new Date());
        order1.setStatus("Order Created");
        orderService.placeOrder(order1);
        Order order2 = new Order();
        order2.setCustomerId(2L);
        order2.setOrderDate(new Date());
        order2.setStatus("Order Created");
        orderService.placeOrder(order2);
        List<Order> orders = orderService.getAllOrders();
        assertTrue(orders.size() >= 2);
    }
}
