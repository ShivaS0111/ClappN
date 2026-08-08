package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplIdorTest {

    @Mock private OrderRepository repository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderAllocationServiceImpl allocationService;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getOrder_deniesForeignStore() {
        OrderEntity entity = new OrderEntity();
        entity.setId(1L);
        entity.setStoreId(99L);
        entity.setTotalAmount(BigDecimal.TEN);
        entity.setOrderDate(LocalDateTime.now());
        entity.setStatus("CREATED");

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doThrow(new AccessDeniedException("You do not have access to store: 99"))
                .when(securityContextService).validateStoreAccess(99L);

        try (MockedStatic<OrderEntityMapper> mapper = mockStatic(OrderEntityMapper.class)) {
            Order domain = new Order();
            domain.setId(1L);
            domain.setStoreId(99L);
            mapper.when(() -> OrderEntityMapper.toModel(entity)).thenReturn(domain);

            assertThrows(AccessDeniedException.class, () -> orderService.getOrder(1L));
        }
    }

    @Test
    void getAllOrders_filtersToAccessibleStores() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(1L));
        when(repository.findByStoreIdIn(List.of(1L))).thenReturn(List.of());

        List<Order> result = orderService.getAllOrders();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findByStoreIdIn(List.of(1L));
        verify(repository, never()).findAll();
    }

    @Test
    void deleteOrder_deniesForeignStore() {
        OrderEntity entity = new OrderEntity();
        entity.setId(3L);
        entity.setStoreId(88L);
        when(repository.findById(3L)).thenReturn(Optional.of(entity));
        doThrow(new AccessDeniedException("denied"))
                .when(securityContextService).validateStoreAccess(88L);

        assertThrows(AccessDeniedException.class, () -> orderService.deleteOrder(3L));
        verify(repository, never()).deleteById(anyLong());
    }
}
