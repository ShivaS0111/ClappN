package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewBusinessRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
public class BusinessDTOMapper {

    public Business toDomain(AddNewBusinessRequest request) {
        return Business.builder()
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .build();
    }

    public Business toDomain(BusinessDTO dto) {
        return Business.builder()
                .id(dto.getId())
                .businessName(dto.getBusinessName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .build();
    }
    public BusinessDTO toDTO(Business business){
        return BusinessDTO.builder()
                .id(business.getId())
                .businessName(business.getBusinessName())
                .description(business.getDescription())
                .status(business.getStatus())
                .build();
    }
}
