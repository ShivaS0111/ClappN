package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicesOfferedByStoreRepository extends JpaRepository<StoreOfferedServiceEntity, Long> {

    void deleteStoreServiceById(Long id);

    Optional<List<StoreOfferedServiceEntity>> findServicesByStoreId(Long id);

}
