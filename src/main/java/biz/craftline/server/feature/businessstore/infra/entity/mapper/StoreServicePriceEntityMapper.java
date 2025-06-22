package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreServicePrice;
import biz.craftline.server.feature.businessstore.infra.entity.StoreServicePriceEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StoreServicePriceEntityMapper {

    StoreServicePrice toDomain(StoreServicePriceEntity dto);
    StoreServicePriceEntity toEntity(StoreServicePrice store);
}
