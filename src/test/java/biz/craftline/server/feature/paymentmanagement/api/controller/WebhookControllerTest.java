package biz.craftline.server.feature.paymentmanagement.api.controller;

import biz.craftline.server.feature.paymentmanagement.application.service.PaymentOrderSyncService;
import biz.craftline.server.feature.paymentmanagement.domain.PaymentProviderFactory;
import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentTransaction;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock private PaymentProviderFactory factory;
    @Mock private PaymentTransactionRepository txRepo;
    @Mock private PaymentProvider provider;
    @Mock private PaymentOrderSyncService orderSyncService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new WebhookController(factory, txRepo, objectMapper, orderSyncService);
    }

    @Test
    void stripeWebhook_rejectsWhenVerificationFails() throws Exception {
        when(factory.providerFor("STRIPE")).thenReturn(provider);
        when(provider.verifyWebhook(anyString(), anyString(), anyMap())).thenReturn(false);

        ResponseEntity<?> response = controller.stripeWebhook("{}", Map.of("stripe-signature", "bad"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(txRepo, never()).save(any());
    }

    @Test
    void stripeWebhook_checkoutSessionCompleted_updatesAndSyncs() throws Exception {
        when(factory.providerFor("STRIPE")).thenReturn(provider);
        when(provider.verifyWebhook(anyString(), anyString(), anyMap())).thenReturn(true);

        String payload = """
                {"type":"checkout.session.completed","data":{"object":{"id":"cs_1","payment_intent":"pi_1","status":"complete"}}}
                """;
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(1L).status("PENDING").providerPaymentId("cs_1").providerOrderId("cs_1").orderId("10").build();
        when(txRepo.findByProviderPaymentId("cs_1")).thenReturn(Optional.of(tx));
        when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.stripeWebhook(payload, Map.of("Stripe-Signature", "sig"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("COMPLETED", tx.getStatus());
        assertEquals("pi_1", tx.getProviderPaymentId());
        verify(orderSyncService).syncFromTransaction(any(), eq("PAID"));
    }

    @Test
    void stripeWebhook_paymentIntentSucceeded() throws Exception {
        when(factory.providerFor("STRIPE")).thenReturn(provider);
        when(provider.verifyWebhook(anyString(), anyString(), anyMap())).thenReturn(true);

        String payload = """
                {"type":"payment_intent.succeeded","data":{"object":{"id":"pi_1","status":"succeeded"}}}
                """;
        PaymentTransaction tx = PaymentTransaction.builder().id(1L).status("PENDING").orderId("10").build();
        when(txRepo.findByProviderPaymentId("pi_1")).thenReturn(Optional.of(tx));
        when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.stripeWebhook(payload, Map.of("Stripe-Signature", "sig"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("COMPLETED", tx.getStatus());
    }

    @Test
    void razorpayWebhook_rejectsWhenVerificationFails() throws Exception {
        when(factory.providerFor("RAZORPAY")).thenReturn(provider);
        when(provider.verifyWebhook(anyString(), anyString(), anyMap())).thenReturn(false);

        ResponseEntity<?> response = controller.razorpayWebhook("{}", Map.of("x-razorpay-signature", "bad"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void razorpayWebhook_captured_linksPaymentId() throws Exception {
        when(factory.providerFor("RAZORPAY")).thenReturn(provider);
        when(provider.verifyWebhook(anyString(), anyString(), anyMap())).thenReturn(true);

        String payload = """
                {"event":"payment.captured","payload":{"payment":{"entity":{"id":"pay_1","order_id":"order_RP_1","status":"captured"}}}}
                """;
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(1L).status("PENDING").providerPaymentId("order_RP_1").providerOrderId("order_RP_1").orderId("10").build();
        when(txRepo.findByProviderPaymentId("pay_1")).thenReturn(Optional.empty());
        when(txRepo.findByProviderOrderId("pay_1")).thenReturn(Optional.empty());
        when(txRepo.findByProviderOrderId("order_RP_1")).thenReturn(Optional.of(tx));
        when(txRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.razorpayWebhook(payload, Map.of("X-Razorpay-Signature", "sig"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("COMPLETED", tx.getStatus());
        assertEquals("pay_1", tx.getProviderPaymentId());
        verify(orderSyncService).syncFromTransaction(any(), eq("PAID"));
    }

    @Test
    void stripeWebhook_unknownPayment_okWithoutSave() throws Exception {
        when(factory.providerFor("STRIPE")).thenReturn(provider);
        when(provider.verifyWebhook(anyString(), anyString(), anyMap())).thenReturn(true);

        String payload = """
                {"type":"payment_intent.succeeded","data":{"object":{"id":"pi_unknown","status":"succeeded"}}}
                """;
        when(txRepo.findByProviderPaymentId("pi_unknown")).thenReturn(Optional.empty());
        when(txRepo.findByProviderOrderId("pi_unknown")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.stripeWebhook(payload, Map.of("stripe-signature", "sig"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(txRepo, never()).save(any());
    }

    @Test
    void razorpayWebhook_missingPaymentEntity_ok() throws Exception {
        when(factory.providerFor("RAZORPAY")).thenReturn(provider);
        when(provider.verifyWebhook(anyString(), anyString(), anyMap())).thenReturn(true);

        ResponseEntity<?> response = controller.razorpayWebhook(
                "{\"event\":\"order.paid\",\"payload\":{}}",
                Map.of("x-razorpay-signature", "sig"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(txRepo, never()).save(any());
    }
}
