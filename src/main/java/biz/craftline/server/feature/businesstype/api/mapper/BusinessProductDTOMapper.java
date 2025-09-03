package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessProductDTO;
import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessProductRequest;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import org.springframework.stereotype.Component;

@Component
public class BusinessProductDTOMapper {

    public BusinessProductDTOMapper(){}

    public BusinessProductDTO toDTO(BusinessProduct domain){
        return new BusinessProductDTO(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getBusinessType(),
                domain.getAmount(),
                domain.getCurrency()
        );
    }

    public BusinessProduct toDomain(BusinessProductDTO dto){
        return new BusinessProduct(
                dto.getId(),
                dto.getName(),
                dto.getDesc(),
                dto.getStatus(),
                dto.getBusinessType(),
                dto.getAmount(),
                dto.getCurrency()
        );
    }

    public BusinessProduct toDomain(AddNewBusinessProductRequest dto){
        return BusinessProduct.builder()
                //.id(dto.getId())
                .name(dto.getName())
                .description(dto.getDesc())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .build();
    }
}
