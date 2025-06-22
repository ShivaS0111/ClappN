package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StoreDTOMapper {

    Store toDomain(StoreDTO dto);
    StoreDTO toDTO(Store store);

}
