package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreEntityMapper {

    @Autowired BusinessEntityMapper mapper;

    public Store toDomain(StoreEntity entity) {
        Business business=null;
        if(entity.getBusiness()!=null){
            business = mapper.toDomain(entity.getBusiness());
        }
        return Store.builder()
                .id(entity.getId())
                .storeName(entity.getStoreName())
                .description(entity.getDescription())
                .business(business)
                .businessType(entity.getBusinessType())
                .build();
    }

    public StoreEntity toEntity(Store store){
        return StoreEntity.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .description(store.getDescription())
                .businessType(store.getBusinessType())
                .build();
    }
}
