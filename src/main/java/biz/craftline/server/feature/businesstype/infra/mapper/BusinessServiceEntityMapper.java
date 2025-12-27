package biz.craftline.server.feature.businesstype.infra.mapper;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusinessServiceEntityMapper {

    @Autowired
    BusinessTypeEntityMapper mapper;

    @Autowired
    CategoryEntityMapper categoryEntityMapper;

    public BusinessServiceEntity toEntity(BusinessService domain) {
        BusinessServiceEntity entity = new BusinessServiceEntity();
        entity.setId(domain.getId());
        entity.setServiceName(domain.getServiceName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus() : 0);
        entity.setAmount(domain.getAmount());
        entity.setBusinessType(mapper.toEntity(domain.getBusinessType()));
        entity.setCurrency(domain.getCurrency());
        List<CategoryEntity> categoryEntityList = domain.getCategories() != null ?
                domain.getCategories().stream().map(c -> categoryEntityMapper.toEntity(c)).toList()
                : null;
        entity.setCategories(categoryEntityList);
        entity.setDuration(domain.getDuration());
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
                entity.getCreatedBy(),
                entity.getCategories() != null ? entity.getCategories()
                        .stream().map(c -> categoryEntityMapper.toDomain(c)).toList() : null,
                entity.getDuration()
        );
    }

    public BusinessService toUpdate(BusinessService old, BusinessService updated) {
        if (updated.getServiceName() != null) old.setServiceName(updated.getServiceName());
        if (updated.getDescription() != null) old.setDescription(updated.getDescription());
        if (updated.getStatus() != null) old.setStatus(updated.getStatus());
        if (updated.getBusinessType() != null) old.setBusinessType(updated.getBusinessType());
        if (updated.getAmount() != null) old.setAmount(updated.getAmount());
        if (updated.getCurrency() != null) old.setCurrency(updated.getCurrency());
        if (updated.getCategories() != null) old.setCategories(updated.getCategories());
        if (updated.getDuration() != null) old.setDuration(updated.getDuration());
        return old;
    }
}
