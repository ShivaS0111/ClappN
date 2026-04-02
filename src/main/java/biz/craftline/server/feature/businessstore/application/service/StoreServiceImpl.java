package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.BusinessEntityJpaRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.UserUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    StoreEntityMapper storeEntityMapper;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    BusinessEntityJpaRepository businessRepository;

    @Autowired
    UserService userService;

    @Autowired
    SecurityContextService securityContextService;

    @Override
    public List<Store> findAll() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            // SYSTEM_ADMIN — unrestricted
            return storeRepository.findAll().stream().map(storeEntityMapper::toDomain).collect(Collectors.toList());
        }
        if (accessibleStoreIds.isEmpty()) {
            return List.of();
        }
        return storeRepository.findAllById(accessibleStoreIds).stream()
                .map(storeEntityMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Store> searchStores(String keyword) {
        List<Store> results = storeRepository.searchStoreByStoreName(keyword).stream()
                .map(storeEntityMapper::toDomain).collect(Collectors.toList());
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            return results; // SYSTEM_ADMIN
        }
        return results.stream()
                .filter(s -> accessibleStoreIds.contains(s.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Store> findStoresByBusiness(long business) {
        securityContextService.validateBusinessAccess(business);
        List<Store> stores = storeRepository.findByBusinessId(business).stream()
                .map(storeEntityMapper::toDomain).collect(Collectors.toList());
        // Further filter by accessible storeIds for business-level users with specific store assignments
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            return stores; // SYSTEM_ADMIN
        }
        return stores.stream()
                .filter(s -> accessibleStoreIds.contains(s.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Store findStoreById(long storeId) {
        securityContextService.validateStoreAccess(storeId);
        StoreEntity storeEntity = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));
        return storeEntityMapper.toDomain(storeEntity);
    }

    @Override
    public void deleteStoreById(Long id) {
        securityContextService.validateStoreAccess(id);
        storeRepository.deleteStoreById(id);
    }

    @Override
    public Optional<Store> findById(Long id) {
        securityContextService.validateStoreAccess(id);
        return storeRepository.findById(id).map(storeEntityMapper::toDomain);
    }

    @Override
    public Store save(Store domain) {
        String currentUsername = UserUtil.requireCurrentUsername();
        Long loggedUserId = userService.getUserByEmail(currentUsername)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));

        // Validate scope: if updating an existing store, check access
        if (domain.getId() != null) {
            securityContextService.validateStoreAccess(domain.getId());
        }
        // If creating under a business, validate business access
        if (domain.getBusiness() != null && domain.getBusiness().getId() != null) {
            securityContextService.validateBusinessAccess(domain.getBusiness().getId());
        }

        StoreEntity entity = storeEntityMapper.toEntity(domain);
        if (domain.getBusiness() != null) {
            Optional<BusinessEntity> businessEntity = businessRepository.findById(domain.getBusiness().getId());
            if (businessEntity.isPresent()) {
                entity.setBusiness(businessEntity.get());
            }
        }
        entity.setCreatedBy(loggedUserId);
        StoreEntity storeEntity = storeRepository.save(entity);
        return storeEntityMapper.toDomain(storeEntity);
    }
}