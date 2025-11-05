package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessProductDTO;
import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessProductRequest;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BusinessProductDTOMapper {

    @Autowired
    CategoryService categoryService;

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
                domain.getCurrency()
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
                dto.getCurrency()
        );
    }

    public BusinessProduct toDomain(AddNewBusinessProductRequest dto) {

        List<Category> categoryList = dto.getCategories() != null ?
                categoryService.findAllByIds(dto.getCategories())
                : new ArrayList<>();
        return BusinessProduct.builder()
                //.id(dto.getId())
                .name(dto.getName())
                .description(dto.getDesc())
                .businessType(dto.getBusinessType() != null ? BusinessType.builder().id(dto.getBusinessType()).build() : null)
                .categories(categoryList)
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .build();
    }
}
