package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreItemPriceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class StoreItemPriceServiceImpl implements StoreItemPriceService {

    @Autowired
    private final ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;

    @Autowired
    StoreItemPriceEntityMapper mapper;

    @Autowired
    StoreItemPriceHandleRepository repository;


    @Override
    public StoreItemPrice save(StoreItemPrice entity) {
        return mapper.toDomain(repository.save(mapper.toEntity(entity)));
    }

    @Override
    public List<StoreItemPrice> findAllByLotId(Long lotId) {
        return findAllByItemIdAndType(lotId, 2L);
    }

    @Override
    public Optional<StoreItemPrice> findByServiceId(Long serviceId) {
        return findByItemIdAndType(serviceId, 1L);
    }

    @Override
    public Optional<StoreItemPrice> findByLotId(Long productLotId) {
        return findByItemIdAndType(productLotId, 2L);
    }

   /* @Override
    public Optional<StoreItemPrice> findByProductId(Long productId) {
        StoreItemPriceEntity storeItemPrice = repository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product Lot Price not configured yet"));
        return Optional.of(mapper.toDomain(storeItemPrice));
    }*/

    @Override
    public List<StoreItemPrice> findAllByServiceId(Long serviceId) {
        return findAllByItemIdAndType(serviceId, 1L);
    }


    @Transactional
    @Override
    public Optional<StoreItemPrice> updateServicePrice(StoreItemPrice itemPrice) {
        return updatePrice(itemPrice, 1L);
    }

    @Transactional
    @Override
    public Optional<StoreItemPrice> updateLotPrice(StoreItemPrice itemPrice) {
        return updatePrice(itemPrice, 2L);
    }

    @Override
    @Cacheable(value = "latestPriceCache", key = "'product_' + #productLotId", unless = "#result == null")
    @Transactional(readOnly = true)
    public StoreItemPrice getLatestPriceForProduct(Long productLotId) {
        return repository.findActivePrices(2L, productLotId, PageRequest.of(0, 1))
                .stream().findFirst().map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("No active price found for product lot " + productLotId));
    }

    @Override
    @Cacheable(value = "latestPriceCache", key = "'service_' + #serviceId", unless = "#result == null")
    @Transactional(readOnly = true)
    public StoreItemPrice getLatestPriceForService(Long serviceId) {
        return repository.findActivePrices(1L, serviceId, PageRequest.of(0, 1))
                .stream().findFirst()
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("No active price found for service " + serviceId));
    }

    @Override
    @Cacheable(value = "latestPriceCache", key = "'product_' + #productLotId + '_qty_' + #quantity", unless = "#result == null")
    @Transactional(readOnly = true)
    public StoreItemPrice getPriceForQuantity(Long productLotId, int quantity) {
        return repository.findBestApplicablePrice(2L, productLotId, quantity, PageRequest.of(0, 1))
                .stream().findFirst().map(mapper::toDomain).orElseGet(() -> getLatestPriceForProduct(productLotId));
    }

    /**
     * Returns the latest saved price for a product (by creation time).
     */
    @Override
    public Optional<StoreItemPriceEntity> getLatestPriceOfProductLotProduct(Long productId) {
        return repository.findLatestPriceByProductId(productId);
    }

    /**
     * Returns the currently valid price (based on validity window).
     */
    @Override
    public Optional<StoreItemPriceEntity> getCurrentPriceOfProductLotProduct(Long productId) {
        return repository.findCurrentPriceByProductId(productId, LocalDateTime.now());
    }

    private Optional<StoreItemPrice> findByItemIdAndType(Long id, Long type) {
        StoreItemPriceEntity storeItemPrice = repository.findByItemIdAndItemType(id, type).orElseThrow(() ->
                new RuntimeException((type == 1L ? "Service" : (type == 2L ? "Product Lot" : "")) +
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
            // Set the validTo to current time to mark it as no longer valid
            storeItemPrice.setValidTo(LocalDateTime.now());
            repository.save(storeItemPrice);
        }
        StoreItemPriceEntity saverEntity = mapper.toEntity(itemPrice);
        StoreItemPriceEntity entity = repository.save(saverEntity);
        return Optional.of(mapper.toDomain(entity));
    }
}
