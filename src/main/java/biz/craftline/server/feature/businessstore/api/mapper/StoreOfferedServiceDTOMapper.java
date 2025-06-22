package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
public class StoreOfferedServiceDTOMapper {

    public StoreOfferedService toDomain(StoreOfferedServiceDTO dto) {
        return StoreOfferedService.builder()
                .id(dto.getId())
                .aliasName(dto.getAliasName())
                .storeId(dto.getStoreId())
                .service(dto.getService())
                .build();
    }

    public StoreOfferedServiceDTO toDTO(StoreOfferedService store) {
        return StoreOfferedServiceDTO.builder()
                .id(store.getId())
                .aliasName(store.getAliasName())
                .storeId(store.getStoreId())
                .service(store.getService())
                .build();
    }
}
