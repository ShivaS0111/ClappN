package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.OrderItemDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.OrderItemDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderItemService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderItemControllerTest {

    @Mock private OrderItemService orderItemService;
    @Mock private OrderItemDTOMapper orderItemDTOMapper;

    @InjectMocks
    private OrderItemController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllOrderItems() {
        OrderItem item = new OrderItem();
        OrderItemDTO dto = new OrderItemDTO();
        when(orderItemService.getAllOrderItems()).thenReturn(List.of(item));
        when(orderItemDTOMapper.toDTO(item)).thenReturn(dto);

        ResponseEntity<APIResponse<List<OrderItemDTO>>> response = controller.getAllOrderItems();
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getOrderItem_foundAndMissing() {
        OrderItem item = new OrderItem();
        OrderItemDTO dto = new OrderItemDTO();
        when(orderItemService.getOrderItem(1L)).thenReturn(item);
        when(orderItemService.getOrderItem(9L)).thenReturn(null);
        when(orderItemDTOMapper.toDTO(item)).thenReturn(dto);

        assertNotNull(controller.getOrderItem(1L).getBody().getData());
        assertNull(controller.getOrderItem(9L).getBody().getData());
    }

    @Test
    void addAndUpdateOrderItem() {
        OrderItemDTO dto = new OrderItemDTO();
        OrderItem domain = new OrderItem();
        OrderItem saved = new OrderItem();
        when(orderItemDTOMapper.fromDTO(dto)).thenReturn(domain);
        when(orderItemService.addOrderItem(domain)).thenReturn(saved);
        when(orderItemService.updateOrderItem(1L, domain)).thenReturn(saved);
        when(orderItemService.updateOrderItem(9L, domain)).thenReturn(null);
        when(orderItemDTOMapper.toDTO(saved)).thenReturn(dto);

        assertNotNull(controller.addOrderItem(dto).getBody().getData());
        assertNotNull(controller.updateOrderItem(1L, dto).getBody().getData());
        assertNull(controller.updateOrderItem(9L, dto).getBody().getData());
    }

    @Test
    void deleteOrderItem() {
        controller.deleteOrderItem(1L);
        verify(orderItemService).deleteOrderItem(1L);
    }
}
