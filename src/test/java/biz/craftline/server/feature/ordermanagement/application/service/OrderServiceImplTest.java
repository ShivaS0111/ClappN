package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.inventorymanagement.domain.service.ProductLotService;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderAllocatedLotEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderItemEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderAllocatedLotRepository;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository repository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderAllocationServiceImpl allocationService;
    @Mock private OrderAllocatedLotRepository allocatedLotRepository;
    @Mock private ProductLotService productLotService;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getAllOrders_adminAndEmptyScope() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(null);
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(orderService.getAllOrders().isEmpty());

        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of());
        assertTrue(orderService.getAllOrders().isEmpty());
    }

    @Test
    void getOrdersByCustomerId_filtersByStore() {
        OrderEntity e1 = entity(1L, 1L);
        OrderEntity e2 = entity(2L, 99L);
        when(repository.findByCustomerId(5L)).thenReturn(List.of(e1, e2));
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(1L));

        List<Order> result = orderService.getOrdersByCustomerId(5L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getOrder_returnsNullWhenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertNull(orderService.getOrder(1L));
    }

    @Test
    void placeOrder1_persists() {
        Order order = new Order();
        order.setStoreId(1L);
        OrderEntity saved = entity(1L, 1L);
        when(repository.save(any())).thenReturn(saved);
        Order result = orderService.placeOrder1(order);
        assertNotNull(result);
    }

    @Test
    void placeOrder_validatesItems() {
        Order order = new Order();
        order.setStoreId(1L);
        order.setItems(List.of());
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(order));
    }

    @Test
    void placeOrder_successWithProductAllocation() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(2);
        item.setPrice(5.0);
        Order order = new Order();
        order.setStoreId(1L);
        order.setItems(List.of(item));

        OrderEntity saved = entity(1L, 1L);
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setId(100L);
        when(repository.save(any())).thenReturn(saved);
        when(orderItemRepository.save(any())).thenReturn(itemEntity);
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of(itemEntity));
        doNothing().when(securityContextService).validateStoreAccess(1L);

        Order result = orderService.placeOrder(order);
        assertNotNull(result);
        verify(allocationService).allocate(1L, 10L, 2, 100L);
    }

    @Test
    void updateOrder_notFoundAndSuccess() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertNull(orderService.updateOrder(1L, new Order()));

        OrderEntity existing = entity(1L, 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);
        doNothing().when(securityContextService).validateStoreAccess(1L);
        Order updated = new Order();
        updated.setStoreId(1L);
        assertNotNull(orderService.updateOrder(1L, updated));
    }

    @Test
    void cancelOrder_setsCancelledStatus() {
        OrderEntity created = entity(1L, 1L);
        created.setStatus("CREATED");
        when(repository.findById(1L)).thenReturn(Optional.of(created));
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.cancelOrder(1L);
        verify(repository).save(argThat(e -> "CANCELLED".equals(e.getStatus())));
    }

    @Test
    void completeOrder_setsCompletedStatus() {
        OrderEntity created = entity(2L, 1L);
        created.setStatus("CREATED");
        when(repository.findById(2L)).thenReturn(Optional.of(created));
        when(orderItemRepository.findByOrder_Id(2L)).thenReturn(List.of());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.completeOrder(2L);
        verify(repository).save(argThat(e -> "COMPLETED".equals(e.getStatus())));
    }

    @Test
    void cancelOrder_invalidStatus() {
        OrderEntity entity = entity(1L, 1L);
        entity.setStatus("COMPLETED");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doNothing().when(securityContextService).validateStoreAccess(1L);
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void releaseAllocations_recordsTransactions() {
        OrderEntity entity = entity(1L, 1L);
        entity.setStatus("CREATED");
        OrderItemEntity item = new OrderItemEntity();
        item.setId(10L);
        OrderAllocatedLotEntity alloc = new OrderAllocatedLotEntity();
        alloc.setProductLotId(5L);
        alloc.setAllocatedQuantity(3);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of(item));
        when(allocatedLotRepository.findByOrderItemIdIn(List.of(10L))).thenReturn(List.of(alloc));
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.completeOrder(1L);
        verify(productLotService, atLeastOnce()).recordTransaction(eq(5L), any(), eq(3), anyString(), anyString(), eq(0L));
    }

    @Test
    void deleteOrder_noOpWhenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        orderService.deleteOrder(1L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteOrder_deniesForeignStore() {
        OrderEntity entity = entity(1L, 88L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doThrow(new AccessDeniedException("denied")).when(securityContextService).validateStoreAccess(88L);
        assertThrows(AccessDeniedException.class, () -> orderService.deleteOrder(1L));
    }

    private static OrderEntity entity(long id, long storeId) {
        OrderEntity e = new OrderEntity();
        e.setId(id);
        e.setStoreId(storeId);
        e.setTotalAmount(BigDecimal.TEN);
        e.setOrderDate(LocalDateTime.now());
        e.setStatus("CREATED");
        return e;
    }
}
