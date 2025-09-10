package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessProductDTO;
import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessProductRequest;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusinessProductDTOMapper {

    @Autowired
    CategoryService categoryService;

    public BusinessProductDTO toDTO(BusinessProduct domain){
        return new BusinessProductDTO(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getCategories(),
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
                dto.getCategories(),
                dto.getAmount(),
                dto.getCurrency()
        );
    }

    public BusinessProduct toDomain(AddNewBusinessProductRequest dto){
        List<Category> categoryList = categoryService.findAllByIds( dto.getCategories());
        return BusinessProduct.builder()
                //.id(dto.getId())
                .name(dto.getName())
                .description(dto.getDesc())
                .categories(categoryList)
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .build();
    }
}
