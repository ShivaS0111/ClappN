package biz.craftline.server.feature.invoicemanagement.application.service;

import biz.craftline.server.config.mail.MailService;
import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.invoicemanagement.infra.mapper.InvoiceEntityMapper;
import biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceItemRepository;
import biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceRepository;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplIdorTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceItemRepository invoiceItemRepository;
    @Mock private InvoiceEntityMapper mapper;
    @Mock private SecurityContextService securityContextService;
    @Mock private OrderRepository orderRepository;
    @Mock private biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository orderItemRepository;
    @Mock private InvoicePdfService invoicePdfService;
    @Mock private MailService mailService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void generate_rejectsWhenOrderStoreMismatch() {
        doNothing().when(securityContextService).validateStoreAccess(1L);
        doNothing().when(securityContextService).validateStoreAccess(2L);

        OrderEntity order = new OrderEntity();
        order.setId(10L);
        order.setStoreId(2L);
        order.setTotalAmount(BigDecimal.ONE);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> invoiceService.generate(10L, 1L));
        assertTrue(ex.getMessage().contains("does not belong"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void generate_rejectsWhenStoreNotAccessible() {
        doThrow(new AccessDeniedException("You do not have access to store: 1"))
                .when(securityContextService).validateStoreAccess(1L);

        assertThrows(AccessDeniedException.class, () -> invoiceService.generate(10L, 1L));
        verify(orderRepository, never()).findById(anyLong());
    }
}
