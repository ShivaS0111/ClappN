package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import org.springframework.stereotype.Component;

@Component
public class BusinessServiceDTOMapper {

    public BusinessServiceDTOMapper(){}

    public BusinessServiceDTO toDTO(BusinessService domain){
        return new BusinessServiceDTO(
                domain.getId(),
                domain.getServiceName(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getBusinessType(),
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
                dto.getBusinessType(),
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
                .currency(dto.getCurrency())
                .build();
    }
}
