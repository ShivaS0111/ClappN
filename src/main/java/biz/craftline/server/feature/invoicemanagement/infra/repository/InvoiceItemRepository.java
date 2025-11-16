package biz.craftline.server.feature.invoicemanagement.infra.repository;


import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItemEntity, Long> {
    List<InvoiceItemEntity> findByInvoiceId(Long invoiceId);
}
