package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StoreOfferedServiceEntityMapper {

    StoreOfferedService toDomain(StoreOfferedServiceEntity dto);
    StoreOfferedServiceEntity toEntity(StoreOfferedService store);
}
