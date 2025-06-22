package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StoreOfferedServiceDTOMapper {

    StoreOfferedService toDomain(StoreOfferedServiceDTO dto);
    StoreOfferedServiceDTO toDTO(StoreOfferedService store);
}
