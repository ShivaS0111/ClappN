package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.dto.BusinessTypeDTO;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;


public class BusinessTypeDTOMapper {

    public BusinessTypeDTO toDTO(BusinessType domain) {
        //var list = domain.services().stream().map(BusinessServiceDTO::toDTO).collect(Collectors.toSet());
        //if( list==null) list = new HashSet<>();
        return new BusinessTypeDTO(domain.getId(), domain.getBusinessName(), domain.getDescription(), domain.getStatus(), new ArrayList<>());
    }

    public BusinessType toDomain(BusinessTypeDTO dto) {
        var list = dto.getServices().stream().map(BusinessServiceDTO::toDomain).toList();
        return new BusinessType(dto.getId(), dto.getBusinessName(), dto.getDescription(), dto.getStatus(), list);
    }
}
