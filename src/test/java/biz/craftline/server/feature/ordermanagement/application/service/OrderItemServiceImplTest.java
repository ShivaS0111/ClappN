package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock private OrderItemRepository repository;
    @Mock private SecurityContextService securityContextService;
    @InjectMocks private OrderItemServiceImpl service;

    private OrderItemEntity itemEntity(long id, long storeId) {
        OrderEntity order = new OrderEntity();
        order.setStoreId(storeId);
        OrderItemEntity e = new OrderItemEntity();
        e.setId(id);
        e.setOrder(order);
        e.setQuantity(1);
        e.setPrice(10.0);
        return e;
    }

    @Test
    void getAllOrderItems_filtersByScope() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(1L));
        when(repository.findAll()).thenReturn(List.of(itemEntity(1L, 1L), itemEntity(2L, 99L)));
        assertEquals(1, service.getAllOrderItems().size());
    }

    @Test
    void getOrderItem_foundAndMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertNull(service.getOrderItem(1L));

        OrderItemEntity entity = itemEntity(1L, 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doNothing().when(securityContextService).validateStoreAccess(1L);
        assertNotNull(service.getOrderItem(1L));
    }

    @Test
    void addUpdateDelete() {
        OrderItem item = new OrderItem();
        item.setQuantity(2);
        item.setPrice(5.0);
        OrderItemEntity saved = itemEntity(1L, 1L);
        when(repository.save(any())).thenReturn(saved);

        assertNotNull(service.addOrderItem(item));

        when(repository.findById(1L)).thenReturn(Optional.of(saved));
        doNothing().when(securityContextService).validateStoreAccess(1L);
        assertNotNull(service.updateOrderItem(1L, item));
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertNull(service.updateOrderItem(9L, item));

        service.deleteOrderItem(1L);
        verify(repository).deleteById(1L);
    }
}
