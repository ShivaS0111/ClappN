package biz.craftline.server.feature.businesstype.infra.mapper;

import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BusinessProductEntityMapper {

    @Autowired
    CategoryEntityMapper mapper;

    @Autowired
    BusinessTypeEntityMapper businessTypeEntityMapper;

    public BusinessProductEntity toEntity(BusinessProduct domain) {
        BusinessProductEntity entity = new BusinessProductEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setAmount(domain.getAmount());
        entity.setCategories(domain.getCategories().stream().map(mapper::toEntity).toList());
        entity.setCurrency(domain.getCurrency());
        return entity;
    }

    public BusinessProduct toDomain(BusinessProductEntity entity) {
        return new BusinessProduct(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getBusinessType()!=null? businessTypeEntityMapper.toDomain(entity.getBusinessType()):null,
                entity.getCategories().stream().map(mapper::toDomain).toList(),
                entity.getAmount(),
                entity.getCreatedBy()
        );
    }
}
