package biz.craftline.server.feature.invoicemanagement.domain.service;


import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;

import java.util.Optional;

public interface InvoiceDomainService {
    Invoice generate(Long orderId, Long storeId);
    Optional<Invoice> findByOrderId(Long orderId);
}
