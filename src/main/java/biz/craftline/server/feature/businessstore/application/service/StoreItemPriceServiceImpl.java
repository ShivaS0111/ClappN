package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.enums.Item;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreItemPriceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.inventorymanagement.infra.repository.ProductLotRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreItemPriceServiceImpl implements StoreItemPriceService {

    private final ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;
    private final ProductsOfferedByStoreRepository productsOfferedByStoreRepository;
    private final ProductLotRepository productLotRepository;
    private final StoreItemPriceEntityMapper mapper;
    private final StoreItemPriceHandleRepository repository;
    private final SecurityContextService securityContextService;

    @Override
    public StoreItemPrice save(StoreItemPrice entity) {
        assertCanAccessItem(entity.getItemId(), entity.getItemType());
        return mapper.toDomain(repository.save(mapper.toEntity(entity)));
    }

    @Override
    public List<StoreItemPrice> findAllByLotId(Long lotId) {
        assertCanAccessLot(lotId);
        return findAllByItemIdAndType(lotId, Item.ProductLot.getType());
    }

    @Override
    public Optional<StoreItemPrice> findByServiceId(Long serviceId) {
        assertCanAccessStoreService(serviceId);
        return findByItemIdAndType(serviceId, Item.SERVICE.getType());
    }

    @Override
    public Optional<StoreItemPrice> findByLotId(Long productLotId) {
        assertCanAccessLot(productLotId);
        return findByItemIdAndType(productLotId, Item.ProductLot.getType());
    }

    @Override
    public List<StoreItemPrice> findAllByServiceId(Long serviceId) {
        assertCanAccessStoreService(serviceId);
        return findAllByItemIdAndType(serviceId, Item.SERVICE.getType());
    }

    @Transactional
    @Override
    public Optional<StoreItemPrice> updateServicePrice(StoreItemPrice itemPrice) {
        if (itemPrice.getItemId() == null) {
            throw new IllegalArgumentException("itemId is required");
        }
        assertCanAccessStoreService(itemPrice.getItemId());
        return updatePrice(itemPrice, Item.SERVICE.getType());
    }

    @Transactional
    @Override
    public Optional<StoreItemPrice> updateLotPrice(StoreItemPrice itemPrice) {
        if (itemPrice.getItemId() == null) {
            throw new IllegalArgumentException("itemId is required");
        }
        assertCanAccessLot(itemPrice.getItemId());
        return updatePrice(itemPrice, Item.ProductLot.getType());
    }

    @Override
    @Cacheable(value = "latestPriceCache", key = "'product_' + #productLotId", unless = "#result == null")
    @Transactional(readOnly = true)
    public StoreItemPrice getLatestPriceForProduct(Long productLotId) {
        assertCanAccessLot(productLotId);
        return repository.findActivePrices(Item.ProductLot.getType(), productLotId, PageRequest.of(0, 1))
                .stream().findFirst().map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("No active price found for product lot " + productLotId));
    }

    private Optional<StoreItemPrice> findByItemIdAndType(Long id, Long type) {
        StoreItemPriceEntity storeItemPrice = repository.findByItemIdAndItemType(id, type).orElseThrow(() ->
                new EntityNotFoundException((type.equals(Item.SERVICE.getType()) ? "Service" : (type.equals(Item.ProductLot.getType()) ? "Product Lot" : "")) +
                        "(" + id + ":" + type + ")" + " Price not configured yet"));
        return Optional.of(mapper.toDomain(storeItemPrice));
    }

    private List<StoreItemPrice> findAllByItemIdAndType(Long serviceId, Long type) {
        return repository.findAllByItemIdAndItemType(serviceId, type).parallelStream().map(mapper::toDomain).toList();
    }

    private Optional<StoreItemPrice> updatePrice(StoreItemPrice itemPrice, Long type) {
        List<StoreItemPriceEntity> storeItemPriceOptional = repository.findByItemIdAndItemTypeOrderByIdDesc(itemPrice.getItemId(), type);

        if (!storeItemPriceOptional.isEmpty()) {
            StoreItemPriceEntity storeItemPrice = storeItemPriceOptional.stream().findFirst().get();
            storeItemPrice.setValidTo(LocalDateTime.now());
            repository.save(storeItemPrice);
        }
        StoreItemPriceEntity saverEntity = mapper.toEntity(itemPrice);
        StoreItemPriceEntity entity = repository.save(saverEntity);
        return Optional.of(mapper.toDomain(entity));
    }

    @Override
    public List<StoreItemPrice> findLatestPricesForProductsInStores(List<Long> ids, Item item) {
        // Bulk enrichment is called only after list queries already filtered by accessible stores.
        // Do not re-resolve each id as a ProductLot (callers often pass store-offered product ids).
        return repository.findLatestPricesForItems(ids, item.getType()).stream().map(mapper::toDomain).toList();
    }

    private void assertCanAccessItem(Long itemId, Long itemType) {
        if (itemId == null || itemType == null) {
            throw new AccessDeniedException("Price item identity is required");
        }
        if (Item.ProductLot.getType().equals(itemType) || Item.PRODUCT.getType().equals(itemType)) {
            assertCanAccessProductPriceItem(itemId);
        } else if (Item.SERVICE.getType().equals(itemType)) {
            assertCanAccessStoreService(itemId);
        } else {
            if (!securityContextService.isSystemAdmin()) {
                throw new AccessDeniedException("Unsupported price item type for scoped access: " + itemType);
            }
        }
    }

    private void assertCanAccessLot(Long lotId) {
        assertCanAccessProductPriceItem(lotId);
    }

    /**
     * Price item ids in this codebase may refer to a ProductLot OR a StoreOfferedProduct.
     * Resolve store ownership from whichever entity exists.
     */
    private void assertCanAccessProductPriceItem(Long itemId) {
        Optional<ProductLotEntity> lot = productLotRepository.findById(itemId);
        if (lot.isPresent()) {
            securityContextService.validateStoreAccess(lot.get().getStoreId());
            return;
        }
        Optional<StoreOfferedProductEntity> product = productsOfferedByStoreRepository.findById(itemId);
        if (product.isPresent()) {
            securityContextService.validateStoreAccess(product.get().getStoreId());
            return;
        }
        throw new EntityNotFoundException("Priced product/lot not found: " + itemId);
    }

    private void assertCanAccessStoreService(Long storeServiceId) {
        StoreOfferedServiceEntity service = servicesOfferedByStoreRepository.findById(storeServiceId)
                .orElseThrow(() -> new EntityNotFoundException("Store service not found: " + storeServiceId));
        securityContextService.validateStoreAccess(service.getStoreId());
    }
}
