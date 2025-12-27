package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedProductDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedProductRequest;
import biz.craftline.server.feature.businessstore.domain.model.Currency;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import org.springframework.stereotype.Component;

@Component
public class StoreOfferedProductDTOMapper {
    public StoreOfferedProductDTO toDTO(StoreOfferedProduct product) {
        StoreOfferedProductDTO dto = new StoreOfferedProductDTO();
        dto.setId(product.getId());
        dto.setAliasName(product.getAliasName());
        dto.setDescription(product.getDescription());
        dto.setStoreId(product.getStoreId());
        dto.setStatus(product.getStatus());
        dto.setBusinessProductId(product.getBusinessProductId());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    public StoreOfferedProduct toDomain(AddNewStoreOfferedProductRequest req) {
        StoreOfferedProduct dto = new StoreOfferedProduct();
        dto.setAliasName(req.getAliasName());
        dto.setDescription(req.getDescription());
        dto.setStatus(req.getStatus());
        dto.setStoreId(req.getStoreId());
        dto.setBusinessProductId(req.getBusinessProductId());
        dto.setCreatedBy(req.getCreatedBy());
        return dto;
    }
}
