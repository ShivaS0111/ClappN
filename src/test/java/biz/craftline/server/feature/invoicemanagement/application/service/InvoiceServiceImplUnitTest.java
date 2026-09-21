package biz.craftline.server.feature.invoicemanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.invoicemanagement.application.enums.InvoiceStatus;
import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;
import biz.craftline.server.feature.invoicemanagement.domain.model.InvoiceItem;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceEntity;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceItemEntity;
import biz.craftline.server.feature.invoicemanagement.infra.mapper.InvoiceEntityMapper;
import biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceItemRepository;
import biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceRepository;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplUnitTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceItemRepository invoiceItemRepository;
    @Mock private InvoiceEntityMapper mapper;
    @Mock private SecurityContextService securityContextService;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private InvoicePdfService invoicePdfService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(securityContextService).validateStoreAccess(anyLong());
    }

    @Test
    void generate_requiresOrderId() {
        assertThrows(IllegalArgumentException.class, () -> invoiceService.generate(null, 1L));
    }

    @Test
    void generate_requiresStoreId() {
        assertThrows(IllegalArgumentException.class, () -> invoiceService.generate(1L, null));
    }

    @Test
    void generate_success() {
        OrderEntity order = new OrderEntity();
        order.setId(10L);
        order.setStoreId(1L);
        order.setTotalAmount(BigDecimal.TEN);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        InvoiceEntity saved = InvoiceEntity.builder().id(5L).orderId(10L).storeId(1L)
                .totalAmount(BigDecimal.TEN).status(InvoiceStatus.GENERATED).build();
        when(invoiceRepository.save(any())).thenReturn(saved);

        OrderItemEntity oi = new OrderItemEntity();
        oi.setItemId(100L);
        oi.setQuantity(2);
        oi.setPrice(5.0);
        when(orderItemRepository.findByOrder_Id(10L)).thenReturn(List.of(oi));
        when(invoiceItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceItemRepository.findByInvoiceId(5L)).thenReturn(List.of());

        Invoice domain = Invoice.builder().id(5L).orderId(10L).storeId(1L).build();
        when(mapper.toDomain(eq(saved), anyList())).thenReturn(domain);

        Invoice result = invoiceService.generate(10L, 1L);
        assertEquals(5L, result.getId());
        verify(invoiceItemRepository).save(any(InvoiceItemEntity.class));
    }

    @Test
    void findByOrderId_present() {
        InvoiceEntity e = InvoiceEntity.builder().id(1L).orderId(10L).storeId(2L).build();
        when(invoiceRepository.findByOrderId(10L)).thenReturn(Optional.of(e));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of());
        Invoice domain = Invoice.builder().id(1L).build();
        when(mapper.toDomain(eq(e), anyList())).thenReturn(domain);

        assertTrue(invoiceService.findByOrderId(10L).isPresent());
        verify(securityContextService).validateStoreAccess(2L);
    }

    @Test
    void getById_notFound() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> invoiceService.getById(1L));
    }

    @Test
    void generate_orderNotFound() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> invoiceService.generate(10L, 1L));
    }

    @Test
    void findByOrderId_empty() {
        when(invoiceRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        assertTrue(invoiceService.findByOrderId(10L).isEmpty());
    }

    @Test
    void getById_success() {
        InvoiceEntity e = InvoiceEntity.builder().id(1L).storeId(2L).build();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(e));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of());
        Invoice domain = Invoice.builder().id(1L).build();
        when(mapper.toDomain(eq(e), anyList())).thenReturn(domain);

        assertEquals(1L, invoiceService.getById(1L).getId());
    }

    @Test
    void generatePdfBytes_delegates() {
        InvoiceEntity e = InvoiceEntity.builder().id(1L).storeId(2L).build();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(e));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of());
        Invoice domain = Invoice.builder().id(1L).build();
        when(mapper.toDomain(eq(e), anyList())).thenReturn(domain);
        when(invoicePdfService.generatePdf(domain)).thenReturn(new byte[]{1, 2, 3});

        byte[] pdf = invoiceService.generatePdfBytes(1L);
        assertArrayEquals(new byte[]{1, 2, 3}, pdf);
    }
}
