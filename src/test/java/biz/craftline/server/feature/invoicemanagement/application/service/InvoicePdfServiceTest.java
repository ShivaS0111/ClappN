package biz.craftline.server.feature.invoicemanagement.application.service;

import biz.craftline.server.feature.invoicemanagement.application.enums.InvoiceStatus;
import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;
import biz.craftline.server.feature.invoicemanagement.domain.model.InvoiceItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoicePdfServiceTest {

    private final InvoicePdfService pdfService = new InvoicePdfService();

    @Test
    void generatePdf_returnsBytes() {
        Invoice invoice = Invoice.builder()
                .id(1L)
                .orderId(10L)
                .storeId(2L)
                .invoiceDate(LocalDateTime.of(2024, 1, 1, 12, 0))
                .status(InvoiceStatus.GENERATED)
                .totalAmount(BigDecimal.valueOf(25.50))
                .items(List.of(InvoiceItem.builder().productId(9L).quantity(2).unitPrice(BigDecimal.TEN).build()))
                .build();

        byte[] pdf = pdfService.generatePdf(invoice);
        assertNotNull(pdf);
        assertTrue(pdf.length > 100);
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
    }

    @Test
    void generatePdf_handlesNullItems() {
        Invoice invoice = Invoice.builder().id(2L).orderId(1L).storeId(1L)
                .status(InvoiceStatus.GENERATED).build();
        byte[] pdf = pdfService.generatePdf(invoice);
        assertTrue(pdf.length > 50);
    }
}
