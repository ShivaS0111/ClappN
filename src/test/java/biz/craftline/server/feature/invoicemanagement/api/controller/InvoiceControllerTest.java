package biz.craftline.server.feature.invoicemanagement.api.controller;

import biz.craftline.server.config.mail.MailService;
import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.invoicemanagement.api.dto.InvoiceRequestDTO;
import biz.craftline.server.feature.invoicemanagement.application.service.InvoiceServiceImpl;
import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceEntity;
import biz.craftline.server.feature.invoicemanagement.infra.mapper.InvoiceEntityMapper;
import biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceItemRepository;
import biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceRepository;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock private InvoiceServiceImpl invoiceService;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceItemRepository invoiceItemRepository;
    @Mock private InvoiceEntityMapper invoiceEntityMapper;
    @Mock private SecurityContextService securityContextService;
    @Mock private MailService mailService;

    @InjectMocks
    private InvoiceController controller;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(securityContextService).validateStoreAccess(anyLong());
    }

    @Test
    void generateInvoice_success() {
        InvoiceRequestDTO req = new InvoiceRequestDTO();
        req.setOrderId(10L);
        req.setStoreId(1L);
        Invoice invoice = Invoice.builder().id(5L).orderId(10L).storeId(1L).build();
        when(invoiceService.generate(10L, 1L)).thenReturn(invoice);

        ResponseEntity<APIResponse<Invoice>> response = controller.generateInvoice(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().getData().getId());
    }

    @Test
    void getByOrder_notFound() {
        when(invoiceService.findByOrderId(10L)).thenReturn(Optional.empty());
        ResponseEntity<APIResponse<Invoice>> response = controller.getByOrder(10L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getByOrder_success() {
        Invoice invoice = Invoice.builder().id(1L).storeId(2L).build();
        when(invoiceService.findByOrderId(10L)).thenReturn(Optional.of(invoice));
        ResponseEntity<APIResponse<Invoice>> response = controller.getByOrder(10L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(securityContextService).validateStoreAccess(2L);
    }

    @Test
    void getAllInvoices_unrestricted() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(null);
        InvoiceEntity e = InvoiceEntity.builder().id(1L).storeId(1L).build();
        when(invoiceRepository.findAll()).thenReturn(List.of(e));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of());
        when(invoiceEntityMapper.toDomain(eq(e), anyList())).thenReturn(Invoice.builder().id(1L).build());

        ResponseEntity<APIResponse<List<Invoice>>> response = controller.getAllInvoices();
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getAllInvoices_emptyAccess_returnsEmpty() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of());
        ResponseEntity<APIResponse<List<Invoice>>> response = controller.getAllInvoices();
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void getById_notFound() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, controller.getById(1L).getStatusCode());
    }

    @Test
    void downloadPdf_returnsBytes() {
        when(invoiceService.generatePdfBytes(1L)).thenReturn(new byte[]{1, 2});
        ResponseEntity<byte[]> response = controller.downloadPdf(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(new byte[]{1, 2}, response.getBody());
    }

    @Test
    void emailInvoice_requiresEmail() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.emailInvoice(1L, Map.of()).getStatusCode());
    }

    @Test
    void emailInvoice_success() throws Exception {
        Invoice invoice = Invoice.builder().id(1L).orderId(10L).build();
        when(invoiceService.getById(1L)).thenReturn(invoice);
        when(invoiceService.generatePdfBytes(1L)).thenReturn(new byte[]{9});
        doNothing().when(mailService).sendWithAttachment(anyString(), anyString(), anyString(), anyString(), any(), anyString());

        ResponseEntity<APIResponse<String>> response = controller.emailInvoice(1L, Map.of("email", "a@b.com"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getByStore_success() {
        InvoiceEntity e = InvoiceEntity.builder().id(1L).storeId(3L).build();
        when(invoiceRepository.findByStoreId(3L)).thenReturn(List.of(e));
        when(invoiceItemRepository.findByInvoiceId(1L)).thenReturn(List.of());
        when(invoiceEntityMapper.toDomain(eq(e), anyList())).thenReturn(Invoice.builder().id(1L).build());

        ResponseEntity<APIResponse<List<Invoice>>> response = controller.getByStore(3L);
        assertEquals(1, response.getBody().getData().size());
        verify(securityContextService).validateStoreAccess(3L);
    }
}
