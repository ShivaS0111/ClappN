package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.enums.Item;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreOfferedServiceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ServicesOfferedByStoreServiceImpl implements ServicesOfferedByStoreService {

    private final StoreOfferedServiceEntityMapper mapper;
    private final ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;
    private final BusinessServicesJpaRepository businessServicesJpaRepository;
    private final StoreService storeService;
    private final UserService userService;
    private final SecurityContextService securityContextService;
    private final biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService storeItemPriceService;
    private final StoreOfferingAssignSupport assignSupport;

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
        return Optional.of(findServicesLatestPrices(list));
    }

    @Override
    @Transactional
    public void deleteStoreServiceById(Long id) {
        findById(id);
        servicesOfferedByStoreRepository.deleteById(id);
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
        List<StoreOfferedServiceEntity> entities = servicesOfferedByStoreRepository
                .searchByStoreIdAndKeyword(storeId.toString(), searchTerm);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public StoreOfferedService save(StoreOfferedService domain) {
        prepareForAssign(domain, null);
        long userId = getCurrentUserId();
        StoreOfferedServiceEntity entity = mapper.toEntity(domain);
        entity.setId(null);
        entity.setCreatedBy(userId);
        return mapper.toDomain(servicesOfferedByStoreRepository.save(entity));
    }

    @Override
    @Transactional
    public List<StoreOfferedService> save(List<StoreOfferedService> domains) {
        return domains.stream().map(this::save).toList();
    }

    @Override
    @Transactional
    public StoreOfferedService update(Long id, StoreOfferedService domain) {
        StoreOfferedServiceEntity existing = servicesOfferedByStoreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store service not found with id: " + id));
        securityContextService.validateStoreAccess(existing.getStoreId());

        domain.setId(id);
        if (domain.getStoreId() == null) {
            domain.setStoreId(existing.getStoreId());
        }
        if (domain.getBusinessServiceId() == null) {
            domain.setBusinessServiceId(existing.getBusinessServiceId());
        }
        prepareForAssign(domain, id);

        long userId = getCurrentUserId();
        StoreOfferedServiceEntity entity = mapper.toEntity(domain);
        entity.setId(id);
        entity.setCreatedBy(existing.getCreatedBy() != null ? existing.getCreatedBy() : userId);
        return mapper.toDomain(servicesOfferedByStoreRepository.save(entity));
    }

    @Override
    @Transactional
    public List<StoreOfferedService> assignToStores(Long businessId, Long businessServiceId,
                                                    List<Long> storeIds, boolean allStores,
                                                    String aliasName, String description, int status) {
        if (businessId == null) {
            throw new IllegalArgumentException("businessId is required");
        }
        if (businessServiceId == null) {
            throw new IllegalArgumentException("catalogItemId / businessServiceId is required");
        }
        securityContextService.validateBusinessAccess(businessId);

        List<Long> targets;
        if (allStores) {
            targets = storeService.findStoresByBusiness(businessId).stream().map(Store::getId).toList();
        } else {
            if (storeIds == null || storeIds.isEmpty()) {
                throw new IllegalArgumentException("storeIds is required unless allStores=true");
            }
            targets = storeIds;
        }
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("No target stores to assign");
        }

        List<StoreOfferedService> created = new java.util.ArrayList<>();
        for (Long storeId : targets) {
            Store store = storeService.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));
            Long storeBiz = store.getBusiness() != null ? store.getBusiness().getId() : null;
            if (storeBiz == null || !storeBiz.equals(businessId)) {
                throw new IllegalArgumentException("Store " + storeId + " is not under business " + businessId);
            }
            if (servicesOfferedByStoreRepository.existsByStoreIdAndBusinessServiceId(storeId, businessServiceId)) {
                continue;
            }
            StoreOfferedService domain = StoreOfferedService.builder()
                    .storeId(storeId)
                    .businessId(businessId)
                    .businessServiceId(businessServiceId)
                    .aliasName(aliasName)
                    .description(description)
                    .status(status)
                    .build();
            created.add(save(domain));
        }
        return created;
    }

    @Override
    @Transactional
    public int unassignFromStores(Long businessId, Long businessServiceId,
                                  List<Long> storeIds, boolean allStores) {
        if (businessId == null) {
            throw new IllegalArgumentException("businessId is required");
        }
        if (businessServiceId == null) {
            throw new IllegalArgumentException("catalogItemId / businessServiceId is required");
        }
        securityContextService.validateBusinessAccess(businessId);

        List<Long> targets;
        if (allStores) {
            targets = storeService.findStoresByBusiness(businessId).stream().map(Store::getId).toList();
        } else {
            if (storeIds == null || storeIds.isEmpty()) {
                throw new IllegalArgumentException("storeIds is required unless allStores=true");
            }
            targets = storeIds;
        }

        int removed = 0;
        for (Long storeId : targets) {
            Store store = storeService.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));
            Long storeBiz = store.getBusiness() != null ? store.getBusiness().getId() : null;
            if (storeBiz == null || !storeBiz.equals(businessId)) {
                throw new IllegalArgumentException("Store " + storeId + " is not under business " + businessId);
            }
            securityContextService.validateStoreAccess(storeId);
            if (servicesOfferedByStoreRepository.existsByStoreIdAndBusinessServiceId(storeId, businessServiceId)) {
                servicesOfferedByStoreRepository.deleteByStoreIdAndBusinessServiceId(storeId, businessServiceId);
                removed++;
            }
        }
        return removed;
    }

    @Override
    public StoreOfferedService findById(Long id) {
        StoreOfferedServiceEntity entity = servicesOfferedByStoreRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Service not found with id: " + id));
        securityContextService.validateStoreAccess(entity.getStoreId());
        StoreOfferedService s = mapper.toDomain(entity);
        try {
            storeItemPriceService.findByServiceId(s.getId()).ifPresent(s::setPrice);
        } catch (Exception ignore) {
        }
        return s;
    }

    public List<StoreOfferedService> findServicesLatestPrices(List<StoreOfferedService> services) {
        if (services == null || services.isEmpty()) {
            return services;
        }
        Map<Long, StoreOfferedService> serviceMap = new HashMap<>(services.size());
        for (StoreOfferedService s : services) {
            serviceMap.put(s.getId(), s);
        }
        List<Long> keys = serviceMap.keySet().stream().toList();
        storeItemPriceService.findLatestPricesForProductsInStores(keys, Item.SERVICE)
                .forEach(price -> {
                    StoreOfferedService svc = serviceMap.get(price.getItemId());
                    if (svc != null) {
                        svc.setPrice(price);
                    }
                });
        return services;
    }

    private void prepareForAssign(StoreOfferedService domain, Long existingOfferingId) {
        if (domain.getBusinessServiceId() == null) {
            throw new IllegalArgumentException("businessServiceId is required");
        }

        Long businessId = assignSupport.resolveAndValidateStoreBusiness(
                domain.getStoreId(), domain.getBusinessId());
        domain.setBusinessId(businessId);

        BusinessServiceEntity master = businessServicesJpaRepository.findById(domain.getBusinessServiceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Business service not found: " + domain.getBusinessServiceId()));

        if (master.getBusinessId() != null && !master.getBusinessId().equals(businessId)) {
            throw new IllegalArgumentException(
                    "Service " + domain.getBusinessServiceId()
                            + " belongs to business " + master.getBusinessId()
                            + ", not store business " + businessId);
        }

        Long catalogTypeId = master.getBusinessType() != null ? master.getBusinessType().getId() : null;
        assignSupport.assertBusinessTypeCompatible(
                assignSupport.storeBusinessTypeId(domain.getStoreId()),
                catalogTypeId,
                "Service");

        servicesOfferedByStoreRepository
                .findByStoreIdAndBusinessServiceId(domain.getStoreId(), domain.getBusinessServiceId())
                .ifPresent(dup -> {
                    if (existingOfferingId == null || !dup.getId().equals(existingOfferingId)) {
                        throw new IllegalArgumentException(
                                "Service " + domain.getBusinessServiceId()
                                        + " is already offered by store " + domain.getStoreId());
                    }
                });
    }

    private Long getCurrentUserId() {
        String currentUsername = UserUtil.requireCurrentUsername();
        return userService.getUserByEmail(currentUsername)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }
}
