package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface StoreProductPriceService {

    StoreItemPrice save(StoreItemPrice entity);

    List<StoreItemPrice> findAllByServiceId(Long serviceId);

    List<StoreItemPrice> findAllByProductLotId(Long productLotId);

    Optional<StoreItemPrice> findByServiceId(Long serviceId);

    Optional<StoreItemPrice> findByProductLotId(Long productLotId);

    //Optional<StoreItemPrice> findByProductId(Long productLotId);

    Optional<StoreItemPrice> updateServicePrice(StoreItemPrice itemPrice);
    Optional<StoreItemPrice> updateProductPrice(StoreItemPrice itemPrice);
    Optional<StoreItemPrice> updateProductLotPrice(StoreItemPrice itemPrice);
}
