package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreEntityMapper {

    @Autowired BusinessEntityMapper mapper;

    public Store toDomain(StoreEntity dto) {
        return Store.builder()
                .id(dto.getId())
                .storeName(dto.getStoreName())
                .description(dto.getDescription())
                .build();
    }

    public StoreEntity toEntity(Store store){
        return StoreEntity.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .description(store.getDescription())
                .build();
    }
}
