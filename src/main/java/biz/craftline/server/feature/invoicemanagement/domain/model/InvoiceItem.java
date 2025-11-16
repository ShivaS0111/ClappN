package biz.craftline.server.feature.invoicemanagement.domain.model;


import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;
}
