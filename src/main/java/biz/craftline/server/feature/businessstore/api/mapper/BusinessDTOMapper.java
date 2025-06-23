package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewBusinessRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.StoreEntityMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BusinessDTOMapper {

    @Lazy
    @Autowired
    StoreDTOMapper storeDTOMapper;

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

    public BusinessDTO toDTO(Business business) {
        Set<StoreDTO> storeDTOS = null;

        if (business.getStores() != null) {
            storeDTOS = business.getStores()
                    .stream()
                    .map(item -> storeDTOMapper.toDTO(item))
                    .collect(Collectors.toSet());
        }

        return BusinessDTO.builder()
                .id(business.getId())
                .businessName(business.getBusinessName())
                .description(business.getDescription())
                .status(business.getStatus())
                .stores(storeDTOS)
                .build();
    }
}
