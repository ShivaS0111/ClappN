package biz.craftline.server.feature.invoicemanagement.domain.model;


import biz.craftline.server.feature.invoicemanagement.application.enums.InvoiceStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    private Long id;
    private Long orderId;
    private Long storeId;
    private BigDecimal totalAmount;
    private LocalDateTime invoiceDate;
    private InvoiceStatus status;
    private List<InvoiceItem> items;
}
