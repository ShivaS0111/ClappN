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
        return BusinessServiceDTO.builder()
                .id(domain.getId())
                .name(domain.getServiceName())
                .desc(domain.getDescription())
                .status(domain.getStatus() != null ? domain.getStatus() : 0)
                .businessId(domain.getBusinessId())
                .businessType(businessTypeDTOMapper.toDTO(domain.getBusinessType()))
                .amount(domain.getAmount())
                .currency(domain.getCurrency())
                .categories(domain.getCategories() != null ? domain.getCategories()
                        .stream().map(c -> {
                            CategoryDTO c1 = categoryDTOMapper.toDTO(c);
                            c1.setChildren(null);
                            return c1;
                        }).toList() : List.of())
                .duration(domain.getDuration())
                .thumbnailUrl(domain.getThumbnailUrl())
                .galleryUrls(domain.getGalleryUrls())
                .build();
    }

    public BusinessService toDomain(BusinessServiceDTO dto) {
        return BusinessService.builder()
                .id(dto.getId())
                .serviceName(dto.getName())
                .description(dto.getDesc())
                .status(dto.getStatus() != null ? dto.getStatus() : 0)
                .businessId(dto.getBusinessId())
                .businessType(businessTypeDTOMapper.toDomain(dto.getBusinessType()))
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .categories(dto.getCategories() != null ? dto.getCategories()
                        .stream().map(c -> {
                            Category c1 = categoryDTOMapper.toDomain(c);
                            c1.setChildren(null);
                            return c1;
                        }).toList() : List.of())
                .duration(dto.getDuration())
                .thumbnailUrl(dto.getThumbnailUrl())
                .galleryUrls(dto.getGalleryUrls())
                .build();
    }

    public BusinessService toDomain(AddNewBusinessServiceRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Service name is required");
        }
        if (dto.getBusinessTypeId() <= 0) {
            throw new IllegalArgumentException("businessTypeId is required");
        }

        List<Category> categoryList = dto.getCategoryIds() != null ?
                categoryService.findAllByIds(dto.getCategoryIds()).stream().peek(c -> c.setChildren(null)).toList()
                : List.of();

        return BusinessService.builder()
                .serviceName(dto.getName())
                .description(dto.getDesc())
                .businessId(dto.getBusinessId())
                .amount(dto.getAmount())
                .businessType(BusinessType.builder().id(dto.getBusinessTypeId()).build())
                .currency(dto.getCurrency())
                .status(dto.getStatus() != null ? dto.getStatus() : 0)
                .categories(categoryList)
                .duration(dto.getDuration())
                .thumbnailUrl(dto.getThumbnailUrl())
                .galleryUrls(dto.getGalleryUrls())
                .build();
    }
}
