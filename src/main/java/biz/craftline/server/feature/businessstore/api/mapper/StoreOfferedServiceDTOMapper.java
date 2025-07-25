package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedServiceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
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

    public StoreOfferedService toDomain(AddNewStoreOfferedServiceRequest req) {
        return StoreOfferedService.builder()
                .aliasName(req.getAliasName())
                .storeId(req.getStoreId())
                .price(StoreItemPrice.builder().id(req.getStoreServicePriceId()).build())
                .service(BusinessService.builder().id(req.getBusinessServiceId()).build())
                .build();
    }
}
