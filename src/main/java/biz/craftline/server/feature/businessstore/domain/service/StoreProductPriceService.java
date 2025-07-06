package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.api.dto.StoreProductPriceDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreProductPrice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface StoreProductPriceService {

    StoreProductPrice save(StoreProductPrice entity);

    List<StoreProductPrice> findAllByProductIdAndProductType(Long productId, Long productType);

    Optional<StoreProductPrice> getConvertedPrice(Long id, String countryCode, String currency);
}
