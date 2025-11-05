package biz.craftline.server.feature.businessstore.application.service;


import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreOfferedPackageService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreOfferedServiceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.util.UserUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class PackagesOfferedByStoreServiceImpl implements StoreOfferedPackageService {

    @Override
    public void deleteStorePackageById(Long id) { }

    @Override
    public Optional<List<StoreOfferedPackage>> findPackagesByStoreId(Long id) {
        return Optional.empty();
    }

    @Override
    public StoreOfferedPackage save(StoreOfferedPackage entity) {
        return null;
    }

    @Override
    public List<StoreOfferedPackage> save(List<StoreOfferedPackage> entity) {
        return List.of();
    }

    @Override
    public StoreOfferedPackage findById(Long id) {
        return null;
    }
}