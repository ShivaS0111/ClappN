package biz.craftline.server.feature.paymentmanagement.api.controller;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import biz.craftline.server.feature.paymentmanagement.domain.service.PaymentService;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerIdorTest {

    @Mock private PaymentService paymentService;
    @Mock private PaymentTransactionRepository txRepo;
    @Mock private OrderRepository orderRepository;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private PaymentController controller;

    @Test
    void status_deniesPaymentForForeignStoreOrder() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(1L)
                .orderId("42")
                .providerPaymentId("pay_abc")
                .status("COMPLETED")
                .build();
        when(txRepo.findByProviderPaymentId("pay_abc")).thenReturn(Optional.of(tx));
        when(securityContextService.isSystemAdmin()).thenReturn(false);

        OrderEntity order = new OrderEntity();
        order.setId(42L);
        order.setStoreId(99L);
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));
        doThrow(new AccessDeniedException("You do not have access to store: 99"))
                .when(securityContextService).validateStoreAccess(99L);

        ResponseEntity<?> response = controller.status("pay_abc");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void status_allowsPaymentForAccessibleStore() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(1L)
                .orderId("42")
                .providerPaymentId("pay_ok")
                .status("COMPLETED")
                .build();
        when(txRepo.findByProviderPaymentId("pay_ok")).thenReturn(Optional.of(tx));
        when(securityContextService.isSystemAdmin()).thenReturn(false);

        OrderEntity order = new OrderEntity();
        order.setId(42L);
        order.setStoreId(1L);
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));
        doNothing().when(securityContextService).validateStoreAccess(1L);

        ResponseEntity<?> response = controller.status("pay_ok");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(tx, response.getBody());
    }
}
