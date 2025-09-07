package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessServiceEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreOfferedServiceEntityMapper {

    @Autowired
    StoreItemPriceEntityMapper entityMapper;
    @Autowired
    BusinessServiceEntityMapper businessServiceEntityMapper;

    public StoreOfferedService toDomain(StoreOfferedServiceEntity dto){
        return StoreOfferedService.builder()
                .id(dto.getId())
                .storeId(dto.getStoreId())
                .service(dto.getServiceId() )
                .aliasName( dto.getAliasName())
                .build();
    }

    public StoreOfferedServiceEntity toEntity(StoreOfferedService store){
        return StoreOfferedServiceEntity.builder()
                .id(store.getId())
                .storeId(store.getStoreId())
                .aliasName( store.getAliasName())
                //.price(entityMapper.toEntity(store.getPrice()))
                .build();
    }
}
