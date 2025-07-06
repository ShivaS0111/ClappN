package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.api.mapper.StoreServicePriceDTOMapper;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessServiceDTOMapper;
import biz.craftline.server.feature.businesstype.infra.entity.mapper.BusinessServiceEntityMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreOfferedServiceEntityMapper {

    @Autowired
    StoreServicePriceEntityMapper entityMapper;
    @Autowired
    BusinessServiceEntityMapper businessServiceEntityMapper;

    public StoreOfferedService toDomain(StoreOfferedServiceEntity dto){
        return StoreOfferedService.builder()
                .id(dto.getId())
                .storeId(dto.getStoreId())
                .service( businessServiceEntityMapper.toDomain(dto.getService()) )
                .aliasName( dto.getAliasName())
                .price(entityMapper.toDomain(dto.getPrice()))
                .build();
    }

    public StoreOfferedServiceEntity toEntity(StoreOfferedService store){
        return StoreOfferedServiceEntity.builder()
                .id(store.getId())
                .storeId(store.getStoreId())
                .aliasName( store.getAliasName())
                .price(entityMapper.toEntity(store.getPrice()))
                .build();
    }
}
