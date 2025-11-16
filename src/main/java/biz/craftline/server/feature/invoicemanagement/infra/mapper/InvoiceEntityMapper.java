package biz.craftline.server.feature.invoicemanagement.infra.mapper;


import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;
import biz.craftline.server.feature.invoicemanagement.domain.model.InvoiceItem;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceEntity;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceItemEntity;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoiceEntityMapper {

    public InvoiceEntity toEntity(Invoice invoice) {
        if (invoice == null) return null;
        return InvoiceEntity.builder()
                .orderId(invoice.getOrderId())
                .storeId(invoice.getStoreId())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .build();
    }

    public InvoiceItemEntity toItemEntity(InvoiceItem item) {
        if (item == null) return null;
        return InvoiceItemEntity.builder()
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }

    public Invoice toDomain(InvoiceEntity e, List<InvoiceItemEntity> items) {
        if (e == null) return null;
        return Invoice.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .storeId(e.getStoreId())
                .totalAmount(e.getTotalAmount())
                .invoiceDate(e.getInvoiceDate())
                .status(e.getStatus())
                .items(items.stream().map(it -> InvoiceItem.builder()
                        .productId(it.getProductId())
                        .quantity(it.getQuantity())
                        .unitPrice(it.getUnitPrice())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
