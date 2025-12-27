package biz.craftline.server.feature.businessstore.domain.service;


import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ServicesOfferedByStoreService {

    void deleteStoreServiceById(Long id);

    Optional<List<StoreOfferedService>> findAll();
    Optional<List<StoreOfferedService>> findServicesByStoreId(Long id);

    StoreOfferedService save(StoreOfferedService entity);

    List<StoreOfferedService> save(List<StoreOfferedService> entity);

    StoreOfferedService findById(Long id);

    List<StoreOfferedService>  searchServiceByKeyword(String searchTerm);

    List<StoreOfferedService>  searchServiceByStoreIdAndKeyword(Long storeId, String searchTerm);
}