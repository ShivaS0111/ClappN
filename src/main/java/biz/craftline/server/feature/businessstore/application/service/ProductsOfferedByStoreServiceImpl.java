package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.enums.Item;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.service.ProductsOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreProductEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
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
public class ProductsOfferedByStoreServiceImpl implements ProductsOfferedByStoreService {

    private final StoreProductEntityMapper mapper;
    private final ProductsOfferedByStoreRepository productsOfferedByStoreRepository;
    private final BusinessProductJpaRepository businessProductJpaRepository;
    private final StoreItemPriceService storeItemPriceService;
    private final StoreService storeService;
    private final UserService userService;
    private final SecurityContextService securityContextService;
    private final StoreOfferingAssignSupport assignSupport;

    @Override
    public List<StoreOfferedProduct> findAll() {
        List<StoreOfferedProduct> list = productsOfferedByStoreRepository.findAll().stream()
                .map(mapper::toDomain).toList();
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds != null) {
            list = list.stream()
                    .filter(p -> p.getStoreId() != null && accessibleStoreIds.contains(p.getStoreId()))
                    .toList();
        }
        return findProductsLatestPrices(list);
    }

    @Override
    @Transactional
    public void deleteStoreProductById(Long id) {
        findById(id);
        productsOfferedByStoreRepository.deleteById(id);
    }

    @Override
    public Optional<List<StoreOfferedProduct>> findProductsByStoreId(Long id) {
        securityContextService.validateStoreAccess(id);
        List<StoreOfferedProductEntity> entities = productsOfferedByStoreRepository.findProductsByStoreId(id)
                .orElse(List.of());
        return Optional.of(entities.stream().map(mapper::toDomain).toList());
    }

    @Override
    public List<StoreOfferedProduct> searchProductByKeyword(String searchTerm) {
        List<StoreOfferedProduct> results = productsOfferedByStoreRepository.searchByKeyword(searchTerm)
                .stream().map(mapper::toDomain).toList();
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds != null) {
            results = results.stream()
                    .filter(p -> p.getStoreId() != null && accessibleStoreIds.contains(p.getStoreId()))
                    .toList();
        }
        return results;
    }

    @Override
    public List<StoreOfferedProduct> searchProductByStoreIdAndKeyword(Long storeId, String searchTerm) {
        securityContextService.validateStoreAccess(storeId);
        List<StoreOfferedProductEntity> entities = productsOfferedByStoreRepository
                .searchByStoreIdAndKeyword(storeId.toString(), searchTerm);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public StoreOfferedProduct save(StoreOfferedProduct domain) {
        prepareForAssign(domain, null);
        long userId = getCurrentUserId();
        StoreOfferedProductEntity entity = mapper.toEntity(domain);
        entity.setId(null);
        entity.setCreatedBy(userId);
        return mapper.toDomain(productsOfferedByStoreRepository.save(entity));
    }

    @Override
    @Transactional
    public List<StoreOfferedProduct> save(List<StoreOfferedProduct> domains) {
        return domains.stream().map(this::save).toList();
    }

    @Override
    @Transactional
    public StoreOfferedProduct update(Long id, StoreOfferedProduct domain) {
        StoreOfferedProductEntity existing = productsOfferedByStoreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store product not found with id: " + id));
        securityContextService.validateStoreAccess(existing.getStoreId());

        domain.setId(id);
        if (domain.getStoreId() == null) {
            domain.setStoreId(existing.getStoreId());
        }
        if (domain.getBusinessProductId() == null) {
            domain.setBusinessProductId(existing.getBusinessProductId());
        }
        prepareForAssign(domain, id);

        long userId = getCurrentUserId();
        StoreOfferedProductEntity entity = mapper.toEntity(domain);
        entity.setId(id);
        entity.setCreatedBy(existing.getCreatedBy() != null ? existing.getCreatedBy() : userId);
        return mapper.toDomain(productsOfferedByStoreRepository.save(entity));
    }

    @Override
    public StoreOfferedProduct findById(Long id) {
        StoreOfferedProductEntity entity = productsOfferedByStoreRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Product not found with id: " + id));
        securityContextService.validateStoreAccess(entity.getStoreId());
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<List<StoreOfferedProduct>> findProductsByBusinessId(Long businessId) {
        securityContextService.validateBusinessAccess(businessId);
        return Optional.of(productsOfferedByStoreRepository.findByBusinessId(businessId)
                .orElse(List.of())
                .stream().map(mapper::toDomain).toList());
    }

    @Override
    @Transactional
    public List<StoreOfferedProduct> assignToStores(Long businessId, Long businessProductId,
                                                    List<Long> storeIds, boolean allStores,
                                                    String aliasName, String description, int status) {
        if (businessId == null) {
            throw new IllegalArgumentException("businessId is required");
        }
        if (businessProductId == null) {
            throw new IllegalArgumentException("catalogItemId / businessProductId is required");
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

        List<StoreOfferedProduct> created = new java.util.ArrayList<>();
        for (Long storeId : targets) {
            Store store = storeService.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));
            Long storeBiz = store.getBusiness() != null ? store.getBusiness().getId() : null;
            if (storeBiz == null || !storeBiz.equals(businessId)) {
                throw new IllegalArgumentException("Store " + storeId + " is not under business " + businessId);
            }
            if (productsOfferedByStoreRepository.existsByStoreIdAndBusinessProductId(storeId, businessProductId)) {
                continue; // already assigned — idempotent
            }
            StoreOfferedProduct domain = StoreOfferedProduct.builder()
                    .storeId(storeId)
                    .businessId(businessId)
                    .businessProductId(businessProductId)
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
    public int unassignFromStores(Long businessId, Long businessProductId,
                                  List<Long> storeIds, boolean allStores) {
        if (businessId == null) {
            throw new IllegalArgumentException("businessId is required");
        }
        if (businessProductId == null) {
            throw new IllegalArgumentException("catalogItemId / businessProductId is required");
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
            if (productsOfferedByStoreRepository.existsByStoreIdAndBusinessProductId(storeId, businessProductId)) {
                productsOfferedByStoreRepository.deleteByStoreIdAndBusinessProductId(storeId, businessProductId);
                removed++;
            }
        }
        return removed;
    }

    public List<StoreOfferedProduct> findProductsLatestPrices(List<StoreOfferedProduct> products) {
        if (products == null || products.isEmpty()) {
            return products;
        }
        Map<Long, StoreOfferedProduct> productMap = new HashMap<>(products.size());
        for (StoreOfferedProduct p : products) {
            productMap.put(p.getId(), p);
        }
        List<Long> keys = productMap.keySet().stream().toList();
        storeItemPriceService
                .findLatestPricesForProductsInStores(keys, Item.ProductLot)
                .forEach(price -> {
                    StoreOfferedProduct product = productMap.get(price.getItemId());
                    if (product != null) {
                        product.setPrice(price);
                    }
                });
        // Fallback: optional business default (amount) when store has no override
        applyBusinessDefaultPrices(products);
        return products;
    }

    private void applyBusinessDefaultPrices(List<StoreOfferedProduct> products) {
        List<Long> masterIds = products.stream()
                .filter(p -> p.getPrice() == null && p.getBusinessProductId() != null)
                .map(StoreOfferedProduct::getBusinessProductId)
                .distinct()
                .toList();
        if (masterIds.isEmpty()) {
            return;
        }
        Map<Long, BusinessProductEntity> masters = new HashMap<>();
        businessProductJpaRepository.findAllById(masterIds).forEach(m -> masters.put(m.getId(), m));
        for (StoreOfferedProduct p : products) {
            if (p.getPrice() != null || p.getBusinessProductId() == null) {
                continue;
            }
            BusinessProductEntity master = masters.get(p.getBusinessProductId());
            if (master == null || master.getAmount() == null) {
                continue;
            }
            p.setPrice(StoreItemPrice.builder()
                    .itemId(p.getId())
                    .itemType(Item.PRODUCT.getType())
                    .price(master.getAmount().doubleValue())
                    .currency(master.getCurrency())
                    .status(1)
                    .build());
        }
    }

    private void prepareForAssign(StoreOfferedProduct domain, Long existingOfferingId) {
        if (domain.getBusinessProductId() == null) {
            throw new IllegalArgumentException("businessProductId is required");
        }

        Long businessId = assignSupport.resolveAndValidateStoreBusiness(
                domain.getStoreId(), domain.getBusinessId());
        domain.setBusinessId(businessId);

        BusinessProductEntity master = businessProductJpaRepository.findById(domain.getBusinessProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Business product not found: " + domain.getBusinessProductId()));

        if (master.getBusinessId() != null && !master.getBusinessId().equals(businessId)) {
            throw new IllegalArgumentException(
                    "Product " + domain.getBusinessProductId()
                            + " belongs to business " + master.getBusinessId()
                            + ", not store business " + businessId);
        }

        Long catalogTypeId = master.getBusinessType() != null ? master.getBusinessType().getId() : null;
        assignSupport.assertBusinessTypeCompatible(
                assignSupport.storeBusinessTypeId(domain.getStoreId()),
                catalogTypeId,
                "Product");

        productsOfferedByStoreRepository
                .findByStoreIdAndBusinessProductId(domain.getStoreId(), domain.getBusinessProductId())
                .ifPresent(dup -> {
                    if (existingOfferingId == null || !dup.getId().equals(existingOfferingId)) {
                        throw new IllegalArgumentException(
                                "Product " + domain.getBusinessProductId()
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
