package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreProductPriceDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreProductPrice;
import org.springframework.stereotype.Component;

@Component
public class StoreProductPriceDTOMapper {

    public StoreProductPrice toDomain(StoreProductPriceDTO dto){
        return StoreProductPrice.builder()
                .id( dto.getId())
                .productId( dto.getServiceId())
                .price( dto.getPrice())
                .currencyId( dto.getCurrencyId())
                .countryId( dto.getCountryId())
                .build();
    }

    public StoreProductPriceDTO toDTO(StoreProductPrice store){
        return StoreProductPriceDTO.builder()
                .id( store.getId())
                .serviceId( store.getProductId())
                .price( store.getPrice())
                .currencyId( store.getCurrencyId())
                .countryId( store.getCountryId())
                .build();
    }
}
