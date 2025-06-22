package biz.craftline.server.feature.businesstype.infra.entity.mapper;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;

public class BusinessServiceEntityMapper {

    public BusinessServiceEntity toEntity(BusinessService domain) {
        BusinessServiceEntity entity = new BusinessServiceEntity();
        entity.setId(domain.getId());
        entity.setServiceName(domain.getServiceName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setAmount(domain.getAmount());
        entity.setBusinessType(domain.getBusinessType());
        entity.setCurrency(domain.getCurrency());
        return entity;
    }

    public BusinessService toDomain(BusinessServiceEntity entity) {
        return new BusinessService(
                entity.getId(),
                entity.getServiceName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getBusinessType(),
                entity.getAmount(),
                entity.getCreatedBy()
        );
    }
}
