package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
public class BusinessEntityMapper {

    @Lazy
    @Autowired
    StoreEntityMapper storeEntityMapper;

    public Business toDomain(BusinessEntity entity){

        Set<Store> stores=null;
        /*if(entity.getStores()!=null) {
            stores =entity.getStores()
                    .stream()
                    .map( item-> storeEntityMapper.toDomain(item))
                    .collect(Collectors.toSet());
        }*/
        return Business.builder()
                .id(entity.getId())
                .businessName(entity.getBusinessName())
                .description(entity.getDescription())
                .stores(stores)
                .build();
    }

    public BusinessEntity toEntity(Business business){
        return BusinessEntity.builder()
                .businessName(business.getBusinessName())
                .description(business.getDescription())
                .build();
    }
}
