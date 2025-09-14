package biz.craftline.server.feature.ordermanagement.infra.repository;

import biz.craftline.server.feature.ordermanagement.infra.entity.BookingDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingDetailsRepository extends JpaRepository<BookingDetailsEntity, Long> {
    // Custom query methods if needed
}

