package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedProductDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreOfferedServiceRequest;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businesstype.api.mapper.BusinessServiceDTOMapper;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreOfferedServiceDTOMapper {

    @Autowired
    BusinessServiceDTOMapper dtoMapper;

    public StoreOfferedService toDomain(StoreOfferedServiceDTO dto) {
        StoreOfferedService service = new StoreOfferedService();
        service.setId(dto.getId());
        service.setAliasName(dto.getAliasName());
        service.setDescription(dto.getDescription());
        service.setStatus(dto.getStatus());
        service.setBusinessServiceId(dto.getBusinessServiceId());
        service.setCreatedBy(dto.getCreatedBy());
        service.setCreatedAt(dto.getCreatedAt());
        service.setUpdatedAt(dto.getUpdatedAt());
        return service;
    }

    public StoreOfferedServiceDTO toDTO(StoreOfferedService service) {

        StoreOfferedServiceDTO dto = new StoreOfferedServiceDTO();
        dto.setId(service.getId());
        dto.setAliasName(service.getAliasName());
        dto.setDescription(service.getDescription());
        dto.setStatus(service.getStatus());
        dto.setStoreId(service.getStoreId());
        dto.setBusinessServiceId(service.getBusinessServiceId());
        dto.setCreatedBy(service.getCreatedBy());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }

    public StoreOfferedService toDomain(AddNewStoreOfferedServiceRequest req) {
        StoreOfferedService service = new StoreOfferedService();
        service.setAliasName(req.getAliasName());
        service.setDescription(req.getDescription());
        service.setStatus(req.getStatus());
        service.setStoreId(req.getStoreId());
        service.setBusinessServiceId(req.getBusinessServiceId());
        return service;
    }
}
