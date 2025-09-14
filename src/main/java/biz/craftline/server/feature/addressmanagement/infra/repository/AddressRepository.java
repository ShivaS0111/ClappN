package biz.craftline.server.feature.addressmanagement.infra.repository;

import biz.craftline.server.feature.addressmanagement.infra.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findByTypeAndReferenceId(String type, Long referenceId);
}

