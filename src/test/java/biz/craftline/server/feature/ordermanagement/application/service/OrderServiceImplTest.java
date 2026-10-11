package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionApplyResult;
import biz.craftline.server.feature.couponmanagement.domain.service.PromotionService;
import biz.craftline.server.feature.ordermanagement.api.dto.DiscountMismatchDetails;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

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
    @Mock private OrderCatalogPriceResolver orderCatalogPriceResolver;
    @Mock private PromotionService promotionService;
    @Mock private StoreRepository storeRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void stubCatalogPrice() {
        // Match production default unless a test overrides
        ReflectionTestUtils.setField(orderService, "acceptLowerClientDiscountDefault", true);
        lenient().when(storeRepository.findById(anyLong())).thenReturn(Optional.empty());
        lenient().when(orderCatalogPriceResolver.resolveUnitPrice(any(), any()))
                .thenReturn(Optional.empty());
        lenient().doAnswer(inv -> {
            var ctx = inv.getArgument(0, biz.craftline.server.feature.couponmanagement.application.service.PromotionOrderContext.class);
            BigDecimal subtotal = BigDecimal.ZERO;
            List<BigDecimal> zeros = new java.util.ArrayList<>();
            if (ctx != null && ctx.getLines() != null) {
                for (var line : ctx.getLines()) {
                    subtotal = subtotal.add(line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
                    zeros.add(BigDecimal.ZERO);
                }
            }
            return PromotionApplyResult.builder()
                    .subtotal(subtotal)
                    .totalLineDiscount(BigDecimal.ZERO)
                    .billDiscount(BigDecimal.ZERO)
                    .totalDiscount(BigDecimal.ZERO)
                    .payable(subtotal)
                    .appliedPromotionIds(List.of())
                    .lineDiscounts(zeros)
                    .build();
        }).when(promotionService).evaluateForOrder(any());
    }

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
        Order order = new Order();
        order.setStoreId(1L);
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(5.0));
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
        assertEquals(5.0, item.getPrice(), 0.001);
    }

    @Test
    void placeOrder_usesServerPriceIgnoresClient() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setPrice(1.0); // client attempt — must be ignored
        Order order = new Order();
        order.setStoreId(1L);
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(99.0));
        OrderEntity saved = entity(1L, 1L);
        when(repository.save(any())).thenReturn(saved);
        when(orderItemRepository.save(any())).thenAnswer(inv -> {
            OrderItemEntity e = inv.getArgument(0);
            assertEquals(99.0, e.getPrice(), 0.001);
            e.setId(100L);
            return e;
        });
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.placeOrder(order);
        assertEquals(99.0, item.getPrice(), 0.001);
    }

    @Test
    void placeOrder_mismatchHigherDiscount_notProceedable() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setDiscountAmount(50.0); // claimed more than entitled (0)
        Order order = new Order();
        order.setStoreId(1L);
        order.setBillDiscount(50.0);
        order.setTotalDiscount(50.0);
        order.setTotalAmount(50.0);
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(100.0));
        doNothing().when(securityContextService).validateStoreAccess(1L);

        DiscountMismatchException ex = assertThrows(DiscountMismatchException.class,
                () -> orderService.placeOrder(order));
        assertFalse(ex.getDetails().isProceedable());
        assertEquals(DiscountMismatchDetails.REASON_CLIENT_HIGHER, ex.getDetails().getReasonCode());
        assertTrue(ex.getDetails().getOptions().stream()
                .anyMatch(o -> DiscountMismatchDetails.OPT_RECONFIGURE.equals(o.getCode())));
        verify(repository, never()).save(any());
    }

    @Test
    void placeOrder_mismatchLowerDiscount_confirmServerFull() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setDiscountAmount(5.0); // less than entitled 10
        Order order = new Order();
        order.setStoreId(1L);
        order.setTotalDiscount(5.0);
        order.setTotalAmount(95.0);
        order.setDiscountResolution("APPLY_SERVER_FULL");
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(100.0));
        doReturn(PromotionApplyResult.builder()
                .subtotal(new BigDecimal("100"))
                .totalLineDiscount(new BigDecimal("10"))
                .billDiscount(BigDecimal.ZERO)
                .totalDiscount(new BigDecimal("10"))
                .payable(new BigDecimal("90"))
                .couponCode("SAVE10")
                .appliedPromotionIds(List.of(1L))
                .lineDiscounts(List.of(new BigDecimal("10")))
                .build()).when(promotionService).evaluateForOrder(any());
        OrderEntity saved = entity(1L, 1L);
        when(repository.save(any())).thenReturn(saved);
        when(orderItemRepository.save(any())).thenAnswer(inv -> {
            OrderItemEntity e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.placeOrder(order);
        assertEquals(10.0, item.getDiscountAmount(), 0.001);
        assertEquals(90.0, order.getTotalAmount(), 0.001);
    }

    @Test
    void placeOrder_mismatchLowerDiscount_confirmClientClaimed() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setDiscountAmount(5.0);
        Order order = new Order();
        order.setStoreId(1L);
        order.setTotalDiscount(5.0);
        order.setTotalAmount(95.0);
        order.setDiscountResolution("APPLY_CLIENT_CLAIMED");
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(100.0));
        doReturn(PromotionApplyResult.builder()
                .subtotal(new BigDecimal("100"))
                .totalLineDiscount(new BigDecimal("10"))
                .billDiscount(BigDecimal.ZERO)
                .totalDiscount(new BigDecimal("10"))
                .payable(new BigDecimal("90"))
                .couponCode("SAVE10")
                .appliedPromotionIds(List.of(1L))
                .lineDiscounts(List.of(new BigDecimal("10")))
                .build()).when(promotionService).evaluateForOrder(any());
        OrderEntity saved = entity(1L, 1L);
        when(repository.save(any())).thenReturn(saved);
        when(orderItemRepository.save(any())).thenAnswer(inv -> {
            OrderItemEntity e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.placeOrder(order);
        assertEquals(5.0, item.getDiscountAmount(), 0.001);
        assertEquals(95.0, order.getTotalAmount(), 0.001);
    }

    @Test
    void resolveAcceptLower_priority_requestStoreBusinessDefault() {
        assertTrue(orderService.resolveAcceptLowerClientDiscount(true, 1L));
        assertFalse(orderService.resolveAcceptLowerClientDiscount(false, 1L));

        StoreEntity store = new StoreEntity();
        store.setId(1L);
        store.setAcceptLowerClientDiscount(false);
        BusinessEntity business = new BusinessEntity();
        business.setAcceptLowerClientDiscount(true);
        store.setBusiness(business);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        assertFalse(orderService.resolveAcceptLowerClientDiscount(null, 1L)); // store wins

        store.setAcceptLowerClientDiscount(null);
        assertTrue(orderService.resolveAcceptLowerClientDiscount(null, 1L)); // business

        business.setAcceptLowerClientDiscount(null);
        assertTrue(orderService.resolveAcceptLowerClientDiscount(null, 1L)); // global default
    }

    @Test
    void placeOrder_storeConfigFalse_forcesAcknowledgeForLowerClaim() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setDiscountAmount(5.0);
        Order order = new Order();
        order.setStoreId(1L);
        order.setTotalDiscount(5.0);
        order.setTotalAmount(95.0);
        order.setItems(List.of(item));

        StoreEntity store = new StoreEntity();
        store.setId(1L);
        store.setAcceptLowerClientDiscount(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(100.0));
        doReturn(PromotionApplyResult.builder()
                .subtotal(new BigDecimal("100"))
                .totalLineDiscount(new BigDecimal("10"))
                .billDiscount(BigDecimal.ZERO)
                .totalDiscount(new BigDecimal("10"))
                .payable(new BigDecimal("90"))
                .appliedPromotionIds(List.of(1L))
                .lineDiscounts(List.of(new BigDecimal("10")))
                .build()).when(promotionService).evaluateForOrder(any());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        DiscountMismatchException ex = assertThrows(DiscountMismatchException.class,
                () -> orderService.placeOrder(order));
        assertTrue(ex.getDetails().isProceedable());
    }

    @Test
    void placeOrder_acceptLowerClientDiscount_skipsAcknowledge() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setDiscountAmount(5.0);
        Order order = new Order();
        order.setStoreId(1L);
        order.setTotalDiscount(5.0);
        order.setTotalAmount(95.0);
        order.setAcceptLowerClientDiscount(true);
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(100.0));
        doReturn(PromotionApplyResult.builder()
                .subtotal(new BigDecimal("100"))
                .totalLineDiscount(new BigDecimal("10"))
                .billDiscount(BigDecimal.ZERO)
                .totalDiscount(new BigDecimal("10"))
                .payable(new BigDecimal("90"))
                .couponCode("SAVE10")
                .appliedPromotionIds(List.of(1L))
                .lineDiscounts(List.of(new BigDecimal("10")))
                .build()).when(promotionService).evaluateForOrder(any());
        OrderEntity saved = entity(1L, 1L);
        when(repository.save(any())).thenReturn(saved);
        when(orderItemRepository.save(any())).thenAnswer(inv -> {
            OrderItemEntity e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.placeOrder(order);
        assertEquals(5.0, item.getDiscountAmount(), 0.001);
        assertEquals(95.0, order.getTotalAmount(), 0.001);
        verify(repository, atLeastOnce()).save(any());
    }

    @Test
    void placeOrder_mismatchLowerDiscount_defaultTrue_placesWithoutAcknowledge() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setDiscountAmount(5.0);
        Order order = new Order();
        order.setStoreId(1L);
        order.setTotalDiscount(5.0);
        order.setTotalAmount(95.0);
        // acceptLowerClientDiscount omitted → uses app default true
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(100.0));
        doReturn(PromotionApplyResult.builder()
                .subtotal(new BigDecimal("100"))
                .totalLineDiscount(new BigDecimal("10"))
                .billDiscount(BigDecimal.ZERO)
                .totalDiscount(new BigDecimal("10"))
                .payable(new BigDecimal("90"))
                .appliedPromotionIds(List.of(1L))
                .lineDiscounts(List.of(new BigDecimal("10")))
                .build()).when(promotionService).evaluateForOrder(any());
        OrderEntity saved = entity(1L, 1L);
        when(repository.save(any())).thenReturn(saved);
        when(orderItemRepository.save(any())).thenAnswer(inv -> {
            OrderItemEntity e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.placeOrder(order);
        assertEquals(5.0, item.getDiscountAmount(), 0.001);
    }

    @Test
    void placeOrder_mismatchLowerDiscount_explicitFalse_returnsOptions() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setDiscountAmount(5.0);
        Order order = new Order();
        order.setStoreId(1L);
        order.setTotalDiscount(5.0);
        order.setTotalAmount(95.0);
        order.setAcceptLowerClientDiscount(false);
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(100.0));
        doReturn(PromotionApplyResult.builder()
                .subtotal(new BigDecimal("100"))
                .totalLineDiscount(new BigDecimal("10"))
                .billDiscount(BigDecimal.ZERO)
                .totalDiscount(new BigDecimal("10"))
                .payable(new BigDecimal("90"))
                .appliedPromotionIds(List.of(1L))
                .lineDiscounts(List.of(new BigDecimal("10")))
                .build()).when(promotionService).evaluateForOrder(any());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        DiscountMismatchException ex = assertThrows(DiscountMismatchException.class,
                () -> orderService.placeOrder(order));
        assertTrue(ex.getDetails().isProceedable());
        assertEquals(DiscountMismatchDetails.REASON_CLIENT_LOWER, ex.getDetails().getReasonCode());
        assertEquals(2, ex.getDetails().getOptions().size());
    }

    @Test
    void placeOrder_acceptsMatchingClientDiscountClaims() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setDiscountAmount(10.0);
        Order order = new Order();
        order.setStoreId(1L);
        order.setCouponCode("SAVE10");
        order.setBillDiscount(0.0);
        order.setTotalDiscount(10.0);
        order.setTotalAmount(90.0);
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.of(100.0));
        doReturn(PromotionApplyResult.builder()
                .subtotal(new BigDecimal("100"))
                .totalLineDiscount(new BigDecimal("10"))
                .billDiscount(BigDecimal.ZERO)
                .totalDiscount(new BigDecimal("10"))
                .payable(new BigDecimal("90"))
                .couponCode("SAVE10")
                .appliedPromotionIds(List.of(1L))
                .lineDiscounts(List.of(new BigDecimal("10")))
                .build()).when(promotionService).evaluateForOrder(any());
        OrderEntity saved = entity(1L, 1L);
        when(repository.save(any())).thenReturn(saved);
        when(orderItemRepository.save(any())).thenAnswer(inv -> {
            OrderItemEntity e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(orderItemRepository.findByOrder_Id(1L)).thenReturn(List.of());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        orderService.placeOrder(order);
        assertEquals(10.0, item.getDiscountAmount(), 0.001);
        assertEquals(90.0, order.getTotalAmount(), 0.001);
        verify(promotionService).redeem(eq(List.of(1L)), eq(1L), any(), eq(1L), any());
    }

    @Test
    void placeOrder_failsWhenNoCatalogPrice() {
        OrderItem item = new OrderItem();
        item.setItemType(1L);
        item.setItemIId(10L);
        item.setQuantity(1);
        item.setPrice(50.0);
        Order order = new Order();
        order.setStoreId(1L);
        order.setItems(List.of(item));

        when(orderCatalogPriceResolver.resolveUnitPrice(eq(1L), any())).thenReturn(Optional.empty());
        doNothing().when(securityContextService).validateStoreAccess(1L);

        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(order));
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
