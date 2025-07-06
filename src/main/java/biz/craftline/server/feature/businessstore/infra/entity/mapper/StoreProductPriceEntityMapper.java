package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreProductPrice;
import biz.craftline.server.feature.businessstore.infra.entity.StoreProductPriceEntity;
import org.springframework.stereotype.Component;

@Component
public class StoreProductPriceEntityMapper {

    public StoreProductPrice toDomain(StoreProductPriceEntity dto){
        return StoreProductPrice.builder()
                .id(dto.getId())
                .productId(dto.getProductId())
                .productType(dto.getProductType())
                .price(dto.getPrice())
                .currencyId(dto.getCurrencyId())
                .build();
    }

    public StoreProductPriceEntity toEntity(StoreProductPrice store){
        return StoreProductPriceEntity.builder()
                //.id(store.getId())
                .productId(store.getProductId())
                .productType(store.getProductType())
                .price(store.getPrice())
                .currencyId(store.getCurrencyId())
                .build();

    }
}
