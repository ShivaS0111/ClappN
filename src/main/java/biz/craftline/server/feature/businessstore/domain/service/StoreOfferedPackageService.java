package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import org.springframework.stereotype.Service;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import java.util.*;

/**
 * Service for managing StoreOfferedPackage entities.
 * Follows standard Spring service conventions.
 */
@Service
public interface StoreOfferedPackageService {

    void deleteStorePackageById(Long id);

    Optional<List<StoreOfferedPackage>> findPackagesByStoreId(Long id);

    StoreOfferedPackage save(StoreOfferedPackage entity);

    List<StoreOfferedPackage> save(List<StoreOfferedPackage> entity);

    StoreOfferedPackage findById(Long id);
}
