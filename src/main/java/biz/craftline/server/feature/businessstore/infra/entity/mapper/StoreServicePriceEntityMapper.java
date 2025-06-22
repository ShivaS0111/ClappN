package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreServicePrice;
import biz.craftline.server.feature.businessstore.infra.entity.StoreServicePriceEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
public class StoreServicePriceEntityMapper {

    public StoreServicePrice toDomain(StoreServicePriceEntity dto){
        return StoreServicePrice.builder()
                .id(dto.getId())
                .serviceId(dto.getServiceId())
                .price(dto.getPrice())
                .currencyId(dto.getCurrencyId())
                .build();
    }

    public StoreServicePriceEntity toEntity(StoreServicePrice store){
        return StoreServicePriceEntity.builder()
                .id(store.getId())
                .serviceId(store.getServiceId())
                .price(store.getPrice())
                .currencyId(store.getCurrencyId())
                .build();

    }
}
