package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessServiceEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class StoreOfferedServiceEntityMapper {

    @Autowired
    StoreItemPriceEntityMapper entityMapper;

    @Autowired
    BusinessServiceEntityMapper businessServiceEntityMapper;

    @Autowired
    BusinessServicesJpaRepository businessServicesJpaRepository;

    public StoreOfferedService toDomain(StoreOfferedServiceEntity dto){
        return StoreOfferedService.builder()
                .id(dto.getId())
                .storeId(dto.getStoreId())
                .service( businessServiceEntityMapper.toDomain( dto.getService()) )
                .aliasName( dto.getAliasName())
                .build();
    }

    public StoreOfferedServiceEntity toEntity(StoreOfferedService store){
        BusinessServiceEntity entity=null;
        if( store.getServiceId()!=null){
            entity = businessServicesJpaRepository.findById(store.getServiceId()).orElse(null);
        }
        return StoreOfferedServiceEntity.builder()
                .id(store.getId())
                .storeId(store.getStoreId())
                .aliasName( store.getAliasName())
                .service(entity)
                //.price(entityMapper.toEntity(store.getPrice()))
                .build();
    }
}
