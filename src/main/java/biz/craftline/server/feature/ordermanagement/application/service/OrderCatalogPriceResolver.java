package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.enums.Item;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Resolves sell price for an order line:
 * store {@code store_item_price} override → business catalog {@code amount} default.
 * <p>
 * Order itemType convention (FE/orderService): 1 = PRODUCT, 2 = VIRTUAL, 3 = SERVICE.
 * Returns empty when no catalog price is configured (caller may keep client price).
 */
@Component
@RequiredArgsConstructor
public class OrderCatalogPriceResolver {

    /** Matches existing placeOrder allocation: itemType 1 = physical product. */
    private static final long ORDER_TYPE_PRODUCT = 1L;
    private static final long ORDER_TYPE_SERVICE = 3L;

    private final ProductsOfferedByStoreRepository productsOfferedByStoreRepository;
    private final ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;
    private final BusinessProductJpaRepository businessProductJpaRepository;
    private final BusinessServicesJpaRepository businessServicesJpaRepository;
    private final StoreItemPriceHandleRepository storeItemPriceHandleRepository;

    public Optional<Double> resolveUnitPrice(Long storeId, OrderItem item) {
        if (storeId == null || item == null || item.getItemIId() == null) {
            return Optional.empty();
        }
        Long type = item.getItemType();
        if (type == null || type == ORDER_TYPE_PRODUCT) {
            return resolveProduct(storeId, item.getItemIId());
        }
        if (type == ORDER_TYPE_SERVICE) {
            return resolveService(storeId, item.getItemIId());
        }
        return Optional.empty();
    }

    private Optional<Double> resolveProduct(Long storeId, Long catalogOrOfferingId) {
        Optional<StoreOfferedProductEntity> offering =
                productsOfferedByStoreRepository.findByStoreIdAndBusinessProductId(storeId, catalogOrOfferingId);
        if (offering.isEmpty()) {
            offering = productsOfferedByStoreRepository.findById(catalogOrOfferingId)
                    .filter(o -> storeId.equals(o.getStoreId()));
        }
        if (offering.isPresent()) {
            Optional<Double> storePrice = activePrice(offering.get().getId(),
                    Item.ProductLot.getType(), Item.PRODUCT.getType());
            if (storePrice.isPresent()) {
                return storePrice;
            }
            Long masterId = offering.get().getBusinessProductId();
            if (masterId != null) {
                return businessProductDefault(masterId);
            }
        }
        return businessProductDefault(catalogOrOfferingId);
    }

    private Optional<Double> resolveService(Long storeId, Long catalogOrOfferingId) {
        Optional<StoreOfferedServiceEntity> offering =
                servicesOfferedByStoreRepository.findByStoreIdAndBusinessServiceId(storeId, catalogOrOfferingId);
        if (offering.isEmpty()) {
            offering = servicesOfferedByStoreRepository.findById(catalogOrOfferingId)
                    .filter(o -> storeId.equals(o.getStoreId()));
        }
        if (offering.isPresent()) {
            Optional<Double> storePrice = activePrice(offering.get().getId(), Item.SERVICE.getType());
            if (storePrice.isPresent()) {
                return storePrice;
            }
            Long masterId = offering.get().getBusinessServiceId();
            if (masterId != null) {
                return businessServiceDefault(masterId);
            }
        }
        return businessServiceDefault(catalogOrOfferingId);
    }

    private Optional<Double> activePrice(Long itemId, Long... itemTypes) {
        for (Long itemType : itemTypes) {
            List<StoreItemPriceEntity> rows = storeItemPriceHandleRepository.findActivePrices(
                    itemType, itemId, PageRequest.of(0, 1));
            if (rows != null && !rows.isEmpty() && rows.get(0).getPrice() != null) {
                return Optional.of(rows.get(0).getPrice());
            }
        }
        return Optional.empty();
    }

    private Optional<Double> businessProductDefault(Long businessProductId) {
        return businessProductJpaRepository.findById(businessProductId)
                .map(BusinessProductEntity::getAmount)
                .map(Float::doubleValue);
    }

    private Optional<Double> businessServiceDefault(Long businessServiceId) {
        return businessServicesJpaRepository.findById(businessServiceId)
                .map(BusinessServiceEntity::getAmount)
                .map(Float::doubleValue);
    }
}
