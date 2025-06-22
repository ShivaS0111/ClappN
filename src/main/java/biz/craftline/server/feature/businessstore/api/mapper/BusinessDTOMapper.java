package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface BusinessDTOMapper {

    Business toDomain(BusinessDTO dto);
    BusinessDTO toDTO(Business business);
}
