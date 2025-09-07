package biz.craftline.server.feature.businesstype.infra.mapper;

import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BusinessProductEntityMapper {

    @Autowired
    CategoryEntityMapper mapper;

    public BusinessProductEntity toEntity(BusinessProduct domain) {
        BusinessProductEntity entity = new BusinessProductEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setAmount(domain.getAmount());
        entity.setCategory(mapper.toEntity(domain.getCategory()));
        entity.setCurrency(domain.getCurrency());
        return entity;
    }

    public BusinessProduct toDomain(BusinessProductEntity entity) {
        return new BusinessProduct(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                mapper.toDomain(entity.getCategory()),
                entity.getAmount(),
                entity.getCreatedBy()
        );
    }
}
