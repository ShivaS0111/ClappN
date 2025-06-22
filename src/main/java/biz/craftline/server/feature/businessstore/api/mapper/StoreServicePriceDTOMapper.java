package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreServicePriceDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreServicePrice;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
public class StoreServicePriceDTOMapper {

    public StoreServicePrice toDomain(StoreServicePriceDTO dto){
        return StoreServicePrice.builder()
                .id( dto.getId())
                .serviceId( dto.getServiceId())
                .price( dto.getPrice())
                .currencyId( dto.getCurrencyId())
                .build();
    }

    public StoreServicePriceDTO toDTO(StoreServicePrice store){
        return StoreServicePriceDTO.builder()
                .id( store.getId())
                .serviceId( store.getServiceId())
                .price( store.getPrice())
                .currencyId( store.getCurrencyId())
                .build();
    }
}
