package biz.craftline.server.feature.invoicemanagement.api.dto;


import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequestDTO {
    private Long orderId;
    private Long storeId;
}
