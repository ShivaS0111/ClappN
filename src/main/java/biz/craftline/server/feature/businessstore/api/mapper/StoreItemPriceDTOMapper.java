package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.request.AddStoreItemPriceRequest;
import biz.craftline.server.feature.businessstore.api.request.UpdateStoreItemPriceRequest;
import biz.craftline.server.feature.businessstore.domain.model.Currency;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreItemPriceDTOMapper {

    @Autowired
    CurrencyMapper currencyMapper;

    public StoreItemPrice toDomain(UpdateStoreItemPriceRequest request) {
        return StoreItemPrice.builder()
                .id(request.getId())
                .productLotId(request.getProductLotId())
                .serviceId(request.getServiceId())
                .price(request.getPrice())
                .currency(request.getCurrencyId())
                .countryId(request.getCountryId())
                .build();
    }

    public StoreItemPrice toDomain(AddStoreItemPriceRequest request) {
        return StoreItemPrice.builder()
                .productLotId(request.getProductLotId())
                .serviceId(request.getServiceId())
                .price(request.getPrice())
                .currency(request.getCurrencyId())
                .countryId(request.getCountryId())
                .build();
    }

    public StoreItemPrice toDomain(StoreItemPriceDTO dto) {
        return StoreItemPrice.builder()
                .id(dto.getId())
                .productLotId(dto.getProductLotId())
                .serviceId(dto.getServiceId())
                .price(dto.getPrice())
                .currency(dto.getCurrency())
                .countryId(dto.getCountryId())
                .build();
    }

    public StoreItemPriceDTO toDTO(StoreItemPrice store) {
        return StoreItemPriceDTO.builder()
                .id(store.getId())
                .serviceId(store.getServiceId())
                .price(store.getPrice())
                .currency(store.getCurrency()).build();
    }
}
