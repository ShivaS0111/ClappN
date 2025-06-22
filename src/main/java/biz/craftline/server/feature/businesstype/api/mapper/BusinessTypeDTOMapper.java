package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.dto.BusinessTypeDTO;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


public class BusinessTypeDTOMapper {

    @Autowired
    BusinessServiceDTOMapper mapper;

    public BusinessTypeDTO toDTO(BusinessType domain) {
        return new BusinessTypeDTO(domain.getId(), domain.getBusinessName(), domain.getDescription(), domain.getStatus(), new ArrayList<>());
    }

    public BusinessType toDomain(BusinessTypeDTO dto) {
        List<BusinessService> list = List.of();
        if( dto.getServices()!=null){
            list=dto.getServices().stream().map(mapper::toDomain).toList();
        }
        return new BusinessType(dto.getId(), dto.getBusinessName(), dto.getDescription(), dto.getStatus(), list);
    }
}
