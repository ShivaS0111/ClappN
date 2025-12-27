package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessProductDTO;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessProductRequest;
import biz.craftline.server.feature.businesstype.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BusinessProductDTOMapper {

    @Lazy
    @Autowired
    BusinessTypeDTOMapper businessTypeDTOMapper;

    public BusinessProductDTO toDTO(BusinessProduct domain) {
        return new BusinessProductDTO(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getStatus(),
                businessTypeDTOMapper.toDTO(domain.getBusinessType()),
                domain.getCategories(),
                domain.getAmount(),
                domain.getCurrency(),
                domain.getBrand()
        );
    }

    public BusinessProduct toDomain(BusinessProductDTO dto) {
        return new BusinessProduct(
                dto.getId(),
                dto.getName(),
                dto.getDesc(),
                dto.getStatus(),
                businessTypeDTOMapper.toDomain(dto.getBusinessType()),
                dto.getCategories(),
                dto.getAmount(),
                dto.getCurrency(),
                dto.getBrand()
        );
    }

    public BusinessProduct toDomain(AddNewBusinessProductRequest dto) {
        return BusinessProduct.builder()
                //.id(dto.getId())
                .name(dto.getName())
                .description(dto.getDesc())
                .businessType(dto.getBusinessTypeId() != null ? BusinessType.builder().id(dto.getBusinessTypeId()).build() : null)
                .brand(dto.getBrandId() != null ? Brand.builder().id(dto.getBrandId()).build() : null)
                .categories(dto.getCategories().stream().map( catId -> Category.builder().id(catId).build()).toList())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .status(dto.getStatus())
                .build();
    }
}
