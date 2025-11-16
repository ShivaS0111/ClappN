package biz.craftline.server.feature.invoicemanagement.application.service;


import biz.craftline.server.feature.invoicemanagement.application.enums.InvoiceStatus;
import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;
import biz.craftline.server.feature.invoicemanagement.domain.service.InvoiceDomainService;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceDomainService {

    private final biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceRepository invoiceRepository;
    private final biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceItemRepository invoiceItemRepository;
    private final biz.craftline.server.feature.invoicemanagement.infra.mapper.InvoiceEntityMapper mapper;

    @Override
    @Transactional
    public Invoice generate(Long orderId, Long storeId) {
        // In standalone mode we do not call order service; caller supplies details or another process
        InvoiceEntity entity = InvoiceEntity.builder()
                .orderId(orderId)
                .storeId(storeId)
                .totalAmount(null)
                .status(InvoiceStatus.GENERATED)
                .build();

        InvoiceEntity saved = invoiceRepository.save(entity);
        return mapper.toDomain(saved, invoiceItemRepository.findByInvoiceId(saved.getId()));
    }

    @Override
    public Optional<Invoice> findByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId).map(e -> mapper.toDomain(e, invoiceItemRepository.findByInvoiceId(e.getId())));
    }
}
