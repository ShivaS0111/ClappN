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
                domain.getStatus()!=null ? domain.getStatus() : 0,
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
                dto.getStatus()!=null ? dto.getStatus() : 0,
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
                .status(dto.getStatus()!=null ? dto.getStatus() : 0)
                .build();
    }
}
