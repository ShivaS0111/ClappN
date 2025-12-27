package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.api.dto.CategoryDTO;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessServiceRequest;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusinessServiceDTOMapper {

    @Lazy
    @Autowired
    BusinessTypeDTOMapper businessTypeDTOMapper;

    @Lazy
    @Autowired
    CategoryDTOMapper categoryDTOMapper;

    @Autowired
    CategoryService categoryService;


    public BusinessServiceDTO toDTO(BusinessService domain) {
        return new BusinessServiceDTO(
                domain.getId(),
                domain.getServiceName(),
                domain.getDescription(),
                domain.getStatus() != null ? domain.getStatus() : 0,
                businessTypeDTOMapper.toDTO(domain.getBusinessType()),
                domain.getAmount(),
                domain.getCurrency(),
                domain.getCategories() != null ? domain.getCategories()
                        .stream().map(c ->{
                            CategoryDTO c1 = categoryDTOMapper.toDTO(c);
                            c1.setChildren(null);
                            return c1;
                        }).toList() : List.of(),
                domain.getDuration()
        );
    }

    public BusinessService toDomain(BusinessServiceDTO dto) {
        return new BusinessService(
                dto.getId(),
                dto.getName(),
                dto.getDesc(),
                dto.getStatus() != null ? dto.getStatus() : 0,
                businessTypeDTOMapper.toDomain(dto.getBusinessType()),
                dto.getAmount(),
                dto.getCurrency(),
                dto.getCategories() != null ? dto.getCategories()
                        .stream().map(c -> {
                            Category c1 = categoryDTOMapper.toDomain(c);
                            c1.setChildren(null);
                            return c1;
                        }).toList() : List.of(),
                dto.getDuration()
        );
    }

    public BusinessService toDomain(AddNewBusinessServiceRequest dto) {

        List<Category> categoryList = dto.getCategoryIds() != null ?
                categoryService.findAllByIds(dto.getCategoryIds()).stream().peek(c -> c.setChildren(null)).toList()
                : List.of();

        return BusinessService.builder()
                //.id(dto.getId())
                .serviceName(dto.getName())
                .description(dto.getDesc())
                .amount(dto.getAmount())
                .businessType(BusinessType.builder().id(dto.getBusinessTypeId()).build())
                .currency(dto.getCurrency())
                .status(dto.getStatus() != null ? dto.getStatus() : 0)
                .categories(categoryList)
                .duration(dto.getDuration())
                .build();
    }
}
