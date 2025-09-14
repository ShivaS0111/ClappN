package biz.craftline.server.feature.businessstore.domain.service;

import org.springframework.stereotype.Service;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import java.util.*;

/**
 * Service for managing StoreOfferedPackage entities.
 * Follows standard Spring service conventions.
 */
@Service
public class StoreOfferedPackageService {
    // In-memory store for demonstration; replace with repository for production
    private final Map<Long, StoreOfferedPackage> packageStore = new HashMap<>();
    private long idCounter = 1;

    /**
     * Returns all store-offered packages.
     * @return list of StoreOfferedPackage
     */
    public List<StoreOfferedPackage> getAllPackages() {
        return new ArrayList<>(packageStore.values());
    }

    /**
     * Returns a package by its ID.
     * @param id package ID
     * @return StoreOfferedPackage or null if not found
     */
    public StoreOfferedPackage getPackage(Long id) {
        return packageStore.get(id);
    }

    /**
     * Saves or updates a package.
     * @param pkg package to save
     * @return saved StoreOfferedPackage
     */
    public StoreOfferedPackage savePackage(StoreOfferedPackage pkg) {
        if (pkg.getId() == null) {
            pkg.setId(idCounter++);
        }
        packageStore.put(pkg.getId(), pkg);
        return pkg;
    }

    /**
     * Deletes a package by its ID.
     * @param id package ID
     */
    public void deletePackage(Long id) {
        packageStore.remove(id);
    }
}
