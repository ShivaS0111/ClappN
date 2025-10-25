package biz.craftline.server.feature.businesstype.infra.mapper;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BusinessServiceEntityMapper {

    @Autowired
    BusinessTypeEntityMapper mapper;

    public BusinessServiceEntity toEntity(BusinessService domain) {
        BusinessServiceEntity entity = new BusinessServiceEntity();
        entity.setId(domain.getId());
        entity.setServiceName(domain.getServiceName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setAmount(domain.getAmount());
        entity.setBusinessType(mapper.toEntity(domain.getBusinessType()));
        entity.setCurrency(domain.getCurrency());
        return entity;
    }

    public BusinessService toDomain(BusinessServiceEntity entity) {
        return new BusinessService(
                entity.getId(),
                entity.getServiceName(),
                entity.getDescription(),
                entity.getStatus(),
                mapper.toDomain(entity.getBusinessType()),
                entity.getAmount(),
                entity.getCreatedBy()
        );
    }
}
