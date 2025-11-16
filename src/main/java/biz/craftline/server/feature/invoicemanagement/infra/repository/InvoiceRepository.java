package biz.craftline.server.feature.invoicemanagement.infra.repository;


import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    Optional<InvoiceEntity> findByOrderId(Long orderId);
}
