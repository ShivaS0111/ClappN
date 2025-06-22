package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreServicePriceDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreServicePrice;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StoreServicePriceDTOMapper {

    StoreServicePrice toDomain(StoreServicePriceDTO dto);

    StoreServicePriceDTO toDTO(StoreServicePrice store);
}
