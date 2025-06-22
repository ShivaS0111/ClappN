package biz.craftline.server.feature.businesstype.infra.entity.mapper;

import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;

import java.util.ArrayList;
import java.util.HashSet;

public class BusinessTypeEntityMapper {

    public BusinessTypeEntity toEntity(BusinessType domain){
        if(domain==null) return null;
        var entity = new BusinessTypeEntity();
        entity.setId(domain.getId());
        entity.setBusinessName(domain.getBusinessName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        return entity;
    }


     public BusinessType toDomain(BusinessTypeEntity entity){
         if(entity==null) return null;
        return new BusinessType(
                entity.getId(),
                entity.getBusinessName(),
                entity.getDescription(),
                entity.getStatus(),
                new ArrayList<>()
        );
    }
}
