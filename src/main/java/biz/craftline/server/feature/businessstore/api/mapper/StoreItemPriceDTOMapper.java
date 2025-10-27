package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreItemPriceDTO;
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
                .currency(
                        Currency.builder()
                                .id(request.getCurrencyId())
                                .build()
                )
                .countryId(request.getCountryId())
                .build();
    }

    public StoreItemPrice toDomain(StoreItemPriceDTO dto) {
        return StoreItemPrice.builder()
                .id(dto.getId())
                .productLotId(dto.getProductLotId())
                .serviceId(dto.getServiceId())
                .itemName(dto.getItemName())
                .price(dto.getPrice())
                .currency(
                        currencyMapper.toDomain(dto.getCurrency()) != null
                                ? currencyMapper.toDomain(dto.getCurrency())
                                : null
                )
                .countryId(dto.getCountryId())
                .status(dto.getStatus())
                .build();
    }

    public StoreItemPriceDTO toDTO(StoreItemPrice store) {
        return StoreItemPriceDTO.builder()
                .id(store.getId())
                .serviceId(store.getServiceId())
                .productLotId(store.getProductLotId())
                .itemName(store.getItemName())
                .price(store.getPrice())
                .currency(
                        store.getCurrency() != null
                                ? currencyMapper.toDTO(store.getCurrency())
                                : null
                )
                .countryId(store.getCountryId())
                .status(store.getStatus())
                .build();
    }
}
