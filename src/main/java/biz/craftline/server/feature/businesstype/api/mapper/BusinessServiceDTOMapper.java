package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class BusinessServiceDTOMapper {

    @Lazy
    @Autowired
    BusinessTypeDTOMapper businessTypeDTOMapper;

    public BusinessServiceDTO toDTO(BusinessService domain){
        return new BusinessServiceDTO(
                domain.getId(),
                domain.getServiceName(),
                domain.getDescription(),
                domain.getStatus(),
                businessTypeDTOMapper.toDTO(domain.getBusinessType()),
                domain.getAmount(),
                domain.getCurrency()
        );
    }

    public BusinessService toDomain(BusinessServiceDTO dto){
        return new BusinessService(
                dto.getId(),
                dto.getName(),
                dto.getDesc(),
                dto.getStatus(),
                businessTypeDTOMapper.toDomain(dto.getBusinessType()),
                dto.getAmount(),
                dto.getCurrency()
        );
    }

    public BusinessService toDomain(AddNewBusinessServiceRequest dto){
        return BusinessService.builder()
                //.id(dto.getId())
                .serviceName(dto.getName())
                .description(dto.getDesc())
                .amount(dto.getAmount())
                .businessType( BusinessType.builder().id(dto.getBusinessType()).build())
                .currency(dto.getCurrency())
                .build();
    }
}
