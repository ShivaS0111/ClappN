package biz.craftline.server.feature.businessstore.infra.entity.mapper;

import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;


@Component
public class BusinessEntityMapper {

    public Business toDomain(BusinessEntity dto){
        return Business.builder()
                .id(dto.getId())
                .businessName(dto.getBusinessName())
                .description(dto.getDescription())
                .build();
    }

    public BusinessEntity toEntity(Business business){
        return BusinessEntity.builder()
                .businessName(business.getBusinessName())
                .description(business.getDescription())
                .build();
    }
}
