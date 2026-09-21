package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.customermanagement.api.mapper.CustomerDTOMapper;
import biz.craftline.server.feature.customermanagement.domain.service.CustomerService;
import biz.craftline.server.feature.ordermanagement.api.dto.OrderDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private StoreService storeService;
    @Mock private StoreDTOMapper storeDTOMapper;
    @Mock private CustomerService customerService;
    @Mock private CustomerDTOMapper customerDTOMapper;

    private OrderController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new OrderController(orderService, storeService, storeDTOMapper,
                customerService, customerDTOMapper);
    }

    @Test
    void listEndpoints() {
        Order order = new Order();
        order.setId(1L);
        order.setStoreId(1L);
        order.setCustomerId(2L);
        when(orderService.getAllOrders()).thenReturn(List.of(order));
        when(orderService.getOrdersByStoreId(1L)).thenReturn(List.of(order));
        when(orderService.getOrdersByCustomerId(2L)).thenReturn(List.of(order));

        assertEquals(1, controller.getAllOrders().getBody().getData().size());
        assertEquals(1, controller.getOrdersByStore(1L).getBody().getData().size());
        assertEquals(1, controller.getOrdersByCustomer(2L).getBody().getData().size());
    }

    @Test
    void getOrder_foundAndMissing() {
        when(orderService.getOrder(1L)).thenReturn(null);
        assertNull(controller.getOrder(1L).getBody().getData());

        Order order = new Order();
        order.setId(1L);
        when(orderService.getOrder(2L)).thenReturn(order);
        assertNotNull(controller.getOrder(2L).getBody().getData());
    }

    @Test
    void placeUpdateCancelComplete() {
        OrderDTO dto = new OrderDTO();
        dto.setStoreId(1L);
        Order saved = new Order();
        saved.setId(1L);
        saved.setStoreId(1L);
        when(orderService.placeOrder(any())).thenReturn(saved);
        assertEquals(HttpStatus.OK, controller.placeOrder(dto).getStatusCode());

        when(orderService.updateOrder(eq(1L), any(Order.class))).thenReturn(saved);
        assertNotNull(controller.updateOrder(1L, dto).getBody().getData());
        when(orderService.updateOrder(eq(9L), any(Order.class))).thenReturn(null);
        assertNull(controller.updateOrder(9L, dto).getBody().getData());

        doNothing().when(orderService).cancelOrder(1L);
        doNothing().when(orderService).completeOrder(1L);
        assertEquals(HttpStatus.OK, controller.cancelOrder(1L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.completeOrder(1L).getStatusCode());
    }

    @Test
    void updateOrderStatus() {
        when(orderService.getOrder(1L)).thenReturn(null);
        assertNull(controller.updateOrderStatus(1L, "SHIPPED").getBody().getData());

        Order order = new Order();
        order.setId(1L);
        order.setStoreId(1L);
        when(orderService.getOrder(2L)).thenReturn(order);
        when(orderService.updateOrder(2L, order)).thenReturn(order);
        assertNotNull(controller.updateOrderStatus(2L, "SHIPPED").getBody().getData());
    }
}
