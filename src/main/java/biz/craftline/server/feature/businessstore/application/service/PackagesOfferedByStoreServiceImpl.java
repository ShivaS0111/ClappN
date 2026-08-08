package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.StoreOfferedPackageService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedPackageEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreOfferedPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackagesOfferedByStoreServiceImpl implements StoreOfferedPackageService {

    private final StoreOfferedPackageRepository repository;
    private final SecurityContextService securityContextService;

    @Override
    @Transactional
    public void deleteStorePackageById(Long id) {
        StoreOfferedPackageEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));
        securityContextService.validateStoreAccess(entity.getStoreId());
        repository.delete(entity);
    }

    @Override
    public Optional<List<StoreOfferedPackage>> findPackagesByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        List<StoreOfferedPackage> list = repository.findByStoreId(storeId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return Optional.of(list);
    }

    @Override
    @Transactional
    public StoreOfferedPackage save(StoreOfferedPackage entity) {
        securityContextService.validateStoreAccess(entity.getStoreId());
        StoreOfferedPackageEntity saved = repository.save(toEntity(entity));
        return toDomain(saved);
    }

    @Override
    @Transactional
    public List<StoreOfferedPackage> save(List<StoreOfferedPackage> entities) {
        return entities.stream().map(this::save).collect(Collectors.toList());
    }

    @Override
    public StoreOfferedPackage findById(Long id) {
        StoreOfferedPackageEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + id));
        securityContextService.validateStoreAccess(entity.getStoreId());
        return toDomain(entity);
    }

    private StoreOfferedPackage toDomain(StoreOfferedPackageEntity e) {
        StoreOfferedPackage pkg = new StoreOfferedPackage();
        pkg.setId(e.getId());
        pkg.setName(e.getName());
        pkg.setDescription(e.getDescription());
        pkg.setStoreId(e.getStoreId());
        pkg.setStatus(e.getStatus());
        pkg.setPrice(e.getPrice());
        pkg.setAvailable(e.getAvailable());
        Set<StoreOfferedProduct> products = new HashSet<>();
        if (e.getProductIds() != null) {
            for (Long pid : e.getProductIds()) {
                StoreOfferedProduct p = new StoreOfferedProduct();
                p.setId(pid);
                products.add(p);
            }
        }
        pkg.setProducts(products);
        Set<StoreOfferedService> services = new HashSet<>();
        if (e.getServiceIds() != null) {
            for (Long sid : e.getServiceIds()) {
                StoreOfferedService s = new StoreOfferedService();
                s.setId(sid);
                services.add(s);
            }
        }
        pkg.setServices(services);
        return pkg;
    }

    private StoreOfferedPackageEntity toEntity(StoreOfferedPackage pkg) {
        StoreOfferedPackageEntity e = new StoreOfferedPackageEntity();
        e.setId(pkg.getId());
        e.setName(pkg.getName());
        e.setDescription(pkg.getDescription());
        e.setStoreId(pkg.getStoreId());
        e.setStatus(pkg.getStatus() != null ? pkg.getStatus() : 1);
        e.setPrice(pkg.getPrice());
        e.setAvailable(pkg.getAvailable() != null ? pkg.getAvailable() : Boolean.TRUE);
        Set<Long> productIds = new HashSet<>();
        if (pkg.getProducts() != null) {
            pkg.getProducts().forEach(p -> {
                if (p.getId() != null) productIds.add(p.getId());
            });
        }
        e.setProductIds(productIds);
        Set<Long> serviceIds = new HashSet<>();
        if (pkg.getServices() != null) {
            pkg.getServices().forEach(s -> {
                if (s.getId() != null) serviceIds.add(s.getId());
            });
        }
        e.setServiceIds(serviceIds);
        return e;
    }
}
