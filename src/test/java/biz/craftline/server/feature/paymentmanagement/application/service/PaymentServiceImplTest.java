package biz.craftline.server.feature.paymentmanagement.application.service;

import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.PaymentProviderFactory;
import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentProviderFactory factory;
    @Mock private PaymentTransactionRepository txRepo;
    @Mock private PaymentOrderSyncService orderSyncService;
    @Mock private PaymentProvider provider;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void initiate_unsupportedGateway_throws() {
        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(1L).amount(100L).currency("INR").gateway("UNKNOWN").build();
        when(factory.providerFor("UNKNOWN")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> paymentService.initiate(req));
    }

    @Test
    void initiate_success_savesTransaction() throws Exception {
        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(1L).amount(500L).currency("INR").gateway("STRIPE").build();
        InitiatePaymentResponse resp = InitiatePaymentResponse.builder()
                .paymentId("cs_1").providerOrderId("cs_1").gateway("STRIPE").build();

        when(factory.providerFor("STRIPE")).thenReturn(provider);
        when(provider.initiatePayment(req)).thenReturn(resp);
        when(txRepo.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        InitiatePaymentResponse result = paymentService.initiate(req);
        assertEquals("cs_1", result.getPaymentId());
        verify(txRepo).save(argThat(tx -> "PENDING".equals(tx.getStatus()) && "1".equals(tx.getOrderId())));
    }

    @Test
    void confirm_notFound() {
        when(txRepo.findByProviderPaymentId("missing")).thenReturn(Optional.empty());
        when(txRepo.findByProviderOrderId("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> paymentService.confirm("missing"));
    }

    @Test
    void confirm_alreadyCompleted_returnsSame() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(1L).providerPaymentId("pi_1").status("COMPLETED").orderId("10").build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));

        PaymentTransaction result = paymentService.confirm("pi_1");
        assertSame(tx, result);
        verify(txRepo, never()).save(any());
    }

    @Test
    void confirm_pending_marksCompletedAndSyncsOrder() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(1L).providerPaymentId("pi_1").status("PENDING")
                .orderId("10").gateway("STRIPE").amount(1000L).build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentTransaction result = paymentService.confirm("pi_1");
        assertEquals("COMPLETED", result.getStatus());
        verify(orderSyncService).syncFromTransaction(any(), eq("PAID"));
    }

    @Test
    void confirm_refundedStatus_throws() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .providerPaymentId("pi_1").status("REFUNDED").build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        assertThrows(IllegalStateException.class, () -> paymentService.confirm("pi_1"));
    }

    @Test
    void refund_full_callsGateway() throws Exception {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(1L).providerPaymentId("pi_1").providerOrderId("cs_1").status("COMPLETED")
                .orderId("10").amount(1000L).gateway("STRIPE").currency("USD").build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        when(factory.providerFor("STRIPE")).thenReturn(provider);
        doNothing().when(provider).refund("pi_1", "cs_1", null, "USD");
        when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentTransaction result = paymentService.refund("pi_1", null);
        assertEquals("REFUNDED", result.getStatus());
        verify(provider).refund("pi_1", "cs_1", null, "USD");
        verify(orderSyncService).syncFromTransaction(any(), eq("REFUNDED"));
    }

    @Test
    void refund_partial() throws Exception {
        PaymentTransaction tx = PaymentTransaction.builder()
                .providerPaymentId("pi_1").providerOrderId("cs_1").status("COMPLETED")
                .amount(1000L).orderId("10").gateway("STRIPE").currency("USD").build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        when(factory.providerFor("STRIPE")).thenReturn(provider);
        doNothing().when(provider).refund(eq("pi_1"), eq("cs_1"), eq(400L), eq("USD"));
        when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentTransaction result = paymentService.refund("pi_1", 400L);
        assertEquals("PARTIALLY_REFUNDED", result.getStatus());
    }

    @Test
    void refund_pending_throws() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .providerPaymentId("pi_1").status("PENDING").amount(1000L).build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        assertThrows(IllegalStateException.class, () -> paymentService.refund("pi_1", 100L));
    }

    @Test
    void initiate_providerThrows_wrapsRuntime() throws Exception {
        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(1L).amount(100L).currency("INR").gateway("STRIPE").build();
        when(factory.providerFor("STRIPE")).thenReturn(provider);
        when(provider.initiatePayment(req)).thenThrow(new RuntimeException("gateway down"));

        assertThrows(RuntimeException.class, () -> paymentService.initiate(req));
    }

    @Test
    void confirm_invalidOrderId_stillCompletes() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(1L).providerPaymentId("pi_1").status("PENDING")
                .orderId("bad").gateway("STRIPE").amount(1000L).build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentTransaction result = paymentService.confirm("pi_1");
        assertEquals("COMPLETED", result.getStatus());
        verify(orderSyncService).syncFromTransaction(any(), eq("PAID"));
    }

    @Test
    void refund_exceedsAmount_throws() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .providerPaymentId("pi_1").status("COMPLETED").amount(100L).build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        assertThrows(IllegalArgumentException.class, () -> paymentService.refund("pi_1", 200L));
    }

    @Test
    void refund_gatewayFailure_propagates() throws Exception {
        PaymentTransaction tx = PaymentTransaction.builder()
                .providerPaymentId("pi_1").providerOrderId("cs_1").status("COMPLETED")
                .amount(100L).gateway("STRIPE").currency("USD").build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        when(factory.providerFor("STRIPE")).thenReturn(provider);
        doThrow(new RuntimeException("stripe down")).when(provider)
                .refund(any(), any(), any(), any());

        assertThrows(RuntimeException.class, () -> paymentService.refund("pi_1", null));
        verify(txRepo, never()).save(any());
    }
}
