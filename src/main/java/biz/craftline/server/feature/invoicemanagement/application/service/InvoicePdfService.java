package biz.craftline.server.feature.invoicemanagement.application.service;

import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;
import biz.craftline.server.feature.invoicemanagement.domain.model.InvoiceItem;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    public byte[] generatePdf(Invoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            document.add(new Paragraph("Invoice #" + invoice.getId(), titleFont));
            document.add(new Paragraph(" ", normal));
            document.add(new Paragraph("Order ID: " + invoice.getOrderId(), normal));
            document.add(new Paragraph("Store ID: " + invoice.getStoreId(), normal));
            if (invoice.getInvoiceDate() != null) {
                document.add(new Paragraph(
                        "Date: " + invoice.getInvoiceDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), normal));
            }
            document.add(new Paragraph("Status: " + invoice.getStatus(), normal));
            document.add(new Paragraph(" ", normal));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f, 1.5f, 1.5f});
            addHeader(table, "Product", headerFont);
            addHeader(table, "Qty", headerFont);
            addHeader(table, "Unit Price", headerFont);
            addHeader(table, "Line Total", headerFont);

            if (invoice.getItems() != null) {
                for (InvoiceItem item : invoice.getItems()) {
                    BigDecimal unit = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                    BigDecimal line = unit.multiply(BigDecimal.valueOf(item.getQuantity()));
                    table.addCell(new Phrase(String.valueOf(item.getProductId()), normal));
                    table.addCell(new Phrase(String.valueOf(item.getQuantity()), normal));
                    table.addCell(new Phrase(unit.toPlainString(), normal));
                    table.addCell(new Phrase(line.toPlainString(), normal));
                }
            }

            document.add(table);
            document.add(new Paragraph(" ", normal));
            BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            document.add(new Paragraph("Total: " + total.toPlainString(), headerFont));

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate invoice PDF", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate invoice PDF", e);
        }
    }

    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        table.addCell(cell);
    }
}
