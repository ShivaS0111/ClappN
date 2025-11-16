package biz.craftline.server.feature.paymentmanagement.infra.repository;

import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfoEntity, Long> {
    // Custom query methods if needed
}

