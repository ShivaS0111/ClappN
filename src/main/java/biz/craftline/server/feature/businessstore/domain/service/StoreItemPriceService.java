package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface StoreItemPriceService {

    StoreItemPrice save(StoreItemPrice entity);


    List<StoreItemPrice> findAllByServiceId(Long serviceId);

    List<StoreItemPrice> findAllByLotId(Long lotId);


    Optional<StoreItemPrice> findByLotId(Long productLotId);
    Optional<StoreItemPrice> findByServiceId(Long productLotId);

    Optional<StoreItemPrice> updateServicePrice(StoreItemPrice itemPrice);

    Optional<StoreItemPrice> updateLotPrice(StoreItemPrice itemPrice);

    StoreItemPrice getLatestPriceForProduct(Long productLotId);


    StoreItemPrice getLatestPriceForService(Long serviceId);

    StoreItemPrice getPriceForQuantity(Long productLotId, int quantity);

    /**
     * Returns the latest saved price for a product (by creation time).
     */
    Optional<StoreItemPriceEntity> getLatestPriceOfProductLotProduct(Long productId);

    /**
     * Returns the currently valid price (based on validity window).
     */
    Optional<StoreItemPriceEntity> getCurrentPriceOfProductLotProduct(Long productId);

}
