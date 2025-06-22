package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StoreEntityMapper {

    Store toDomain(StoreEntity dto);
    StoreEntity toEntity(Store store);
}
