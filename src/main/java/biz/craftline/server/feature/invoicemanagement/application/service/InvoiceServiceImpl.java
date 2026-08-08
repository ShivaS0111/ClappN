package biz.craftline.server.feature.invoicemanagement.application.service;


import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.invoicemanagement.application.enums.InvoiceStatus;
import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;
import biz.craftline.server.feature.invoicemanagement.domain.service.InvoiceDomainService;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceEntity;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceItemEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceDomainService {

    private final biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceRepository invoiceRepository;
    private final biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceItemRepository invoiceItemRepository;
    private final biz.craftline.server.feature.invoicemanagement.infra.mapper.InvoiceEntityMapper mapper;
    private final SecurityContextService securityContextService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InvoicePdfService invoicePdfService;

    @Override
    @Transactional
    public Invoice generate(Long orderId, Long storeId) {
        securityContextService.validateStoreAccess(storeId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        securityContextService.validateStoreAccess(order.getStoreId());
        if (order.getStoreId() != null && !order.getStoreId().equals(storeId)) {
            throw new AccessDeniedException("Order does not belong to store: " + storeId);
        }

        InvoiceEntity entity = InvoiceEntity.builder()
                .orderId(orderId)
                .storeId(storeId)
                .totalAmount(order.getTotalAmount())
                .status(InvoiceStatus.GENERATED)
                .build();

        InvoiceEntity saved = invoiceRepository.save(entity);

        List<OrderItemEntity> orderItems = orderItemRepository.findByOrder_Id(orderId);
        for (OrderItemEntity oi : orderItems) {
            invoiceItemRepository.save(InvoiceItemEntity.builder()
                    .invoiceId(saved.getId())
                    .productId(oi.getItemId() != null ? oi.getItemId() : 0L)
                    .quantity(oi.getQuantity())
                    .unitPrice(BigDecimal.valueOf(oi.getPrice()))
                    .build());
        }

        return mapper.toDomain(saved, invoiceItemRepository.findByInvoiceId(saved.getId()));
    }

    @Override
    public Optional<Invoice> findByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId).map(e -> {
            securityContextService.validateStoreAccess(e.getStoreId());
            return mapper.toDomain(e, invoiceItemRepository.findByInvoiceId(e.getId()));
        });
    }

    public Invoice getById(Long id) {
        InvoiceEntity entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
        securityContextService.validateStoreAccess(entity.getStoreId());
        return mapper.toDomain(entity, invoiceItemRepository.findByInvoiceId(entity.getId()));
    }

    public byte[] generatePdfBytes(Long id) {
        return invoicePdfService.generatePdf(getById(id));
    }
}
