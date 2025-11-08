package biz.craftline.server.feature.inventorymanagement.domain.model;

import biz.craftline.server.feature.inventorymanagement.application.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductLotTransaction {
    private Long id;

    private Long productLotId;

    private TransactionType type; // BLOCK, UNBLOCK, SOLD, RETURN, ADJUST

    private int quantityChange;

    private int beforeQuantity;

    private int afterQuantity;

    private BigDecimal unitPrice;

    private String reason;

    private String referenceId; // e.g. ORDER_ID, RESERVATION_ID

    private String correlationId; // for idempotency / distributed tracing

    private long performedBy;

}
