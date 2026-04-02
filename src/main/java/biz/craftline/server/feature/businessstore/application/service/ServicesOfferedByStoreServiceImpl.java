package biz.craftline.server.feature.businessstore.application.service;


import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreOfferedServiceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.UserUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ServicesOfferedByStoreServiceImpl implements ServicesOfferedByStoreService {

    @Autowired
    StoreOfferedServiceEntityMapper mapper;

    @Autowired
    ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;

    @Autowired
    BusinessServicesJpaRepository businessServicesJpaRepository;

    @Autowired
    UserService userService;

    @Autowired
    SecurityContextService securityContextService;

    @Override
    public Optional<List<StoreOfferedService>> findAll() {
        List<StoreOfferedService> list = servicesOfferedByStoreRepository.findAll()
                .stream().map(mapper::toDomain).toList();
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds != null) {
            list = list.stream()
                    .filter(s -> s.getStoreId() != null && accessibleStoreIds.contains(s.getStoreId()))
                    .toList();
        }
        return Optional.of(list);
    }

    @Override
    public void deleteStoreServiceById(Long id) {
        servicesOfferedByStoreRepository.deleteStoreServiceById(id);
    }


    @Override
    public Optional<List<StoreOfferedService>> findServicesByBusinessId(Long businessId) {
        securityContextService.validateBusinessAccess(businessId);
        List<StoreOfferedServiceEntity> entities = servicesOfferedByStoreRepository
                .findByBusinessId(businessId).orElse(List.of());
        return Optional.of(entities.stream().map(mapper::toDomain).toList());
    }

    @Override
    public Optional<List<StoreOfferedService>> findServicesByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        List<StoreOfferedServiceEntity> entities = servicesOfferedByStoreRepository.findByStoreId(storeId).orElse(List.of());
        return Optional.of(entities.stream().map(mapper::toDomain).toList());
    }

    @Override
    public List<StoreOfferedService> searchServiceByKeyword(String searchTerm) {
        List<StoreOfferedService> results = servicesOfferedByStoreRepository.searchByKeyword(searchTerm)
                .stream().map(mapper::toDomain).toList();
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds != null) {
            results = results.stream()
                    .filter(s -> s.getStoreId() != null && accessibleStoreIds.contains(s.getStoreId()))
                    .toList();
        }
        return results;
    }

    @Override
    public List<StoreOfferedService> searchServiceByStoreIdAndKeyword(Long storeId, String searchTerm) {
        securityContextService.validateStoreAccess(storeId);
        List<StoreOfferedServiceEntity> entities = servicesOfferedByStoreRepository.searchByStoreIdAndKeyword(storeId.toString(), searchTerm);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public StoreOfferedService save(StoreOfferedService domain) {
        if (domain.getStoreId() != null) {
            securityContextService.validateStoreAccess(domain.getStoreId());
        }
        long userId = getCurrentUserId();
        StoreOfferedServiceEntity entity = mapper.toEntity(domain);
        entity.setCreatedBy(userId);
        StoreOfferedServiceEntity en = servicesOfferedByStoreRepository.save(entity);
        return mapper.toDomain(en);
    }

    @Override
    public List<StoreOfferedService> save(List<StoreOfferedService> domains) {
        long userId = getCurrentUserId();
        List<StoreOfferedServiceEntity> entities = domains.stream().map(domain -> {
                    if (domain.getStoreId() != null) {
                        securityContextService.validateStoreAccess(domain.getStoreId());
                    }
                    StoreOfferedServiceEntity entity = mapper.toEntity(domain);
                    entity.setCreatedBy(userId);
                    return entity;
                }

        ).toList();
        List<StoreOfferedServiceEntity> en = servicesOfferedByStoreRepository.saveAll(entities);
        return en.stream().map(mapper::toDomain).toList();
    }

    @Override
    public StoreOfferedService findById(Long id) {
        Optional<StoreOfferedServiceEntity> service = servicesOfferedByStoreRepository.findById(id);
        service.orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        return mapper.toDomain(service.get());
    }

    private Long getCurrentUserId() {
        String currentUsername = UserUtil.requireCurrentUsername();
        return userService.getUserByEmail(currentUsername)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }

}