package biz.craftline.server.feature.businessstore.domain.service;


import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ServicesOfferedByStoreService {

    void deleteStoreServiceById(Long id);

    Optional<List<StoreOfferedService>> findServicesByStoreId(Long id);

    StoreOfferedService save(StoreOfferedService entity);

    StoreOfferedService findById(Long id);
}