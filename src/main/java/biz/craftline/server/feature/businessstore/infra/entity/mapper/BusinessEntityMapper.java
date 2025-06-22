package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface BusinessEntityMapper {

    Business toDomain(BusinessEntity dto);
    BusinessEntity toEntity(Business business);
}
