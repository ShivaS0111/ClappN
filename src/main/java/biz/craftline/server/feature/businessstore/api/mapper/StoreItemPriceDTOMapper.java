package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
import biz.craftline.server.feature.businessstore.api.request.AddStoreItemPriceRequest;
import biz.craftline.server.feature.businessstore.api.request.UpdateStoreItemPriceRequest;
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
                .itemType(request.getProductLotId())
                .itemId(request.getServiceId())
                .price(request.getPrice())
                .currency(request.getCurrencyId())
                .countryId(request.getCountryId())
                .build();
    }

    public StoreItemPrice toDomain(AddStoreItemPriceRequest request) {
        return StoreItemPrice.builder()
                .itemType(request.getItemType())
                .itemId(request.getItemId())
                .price(request.getPrice())
                .currency(request.getCurrencyId())
                .countryId(request.getCountryId())
                .build();
    }

    public StoreItemPrice toDomain(StoreItemPriceDTO dto) {
        return StoreItemPrice.builder()
                .id(dto.getId())
                .itemType(dto.getItemType())
                .itemId(dto.getItemId())
                .price(dto.getPrice())
                .currency(dto.getCurrency())
                .countryId(dto.getCountryId())
                .build();
    }

    public StoreItemPriceDTO toDTO(StoreItemPrice store) {
        return StoreItemPriceDTO.builder()
                .id(store.getId())
                .itemType(store.getItemType())
                .itemId(store.getItemId())
                .price(store.getPrice())
                .currency(store.getCurrency()).build();
    }
}
