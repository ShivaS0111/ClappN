package biz.craftline.server.feature.businesstype.api.mapper;

import biz.craftline.server.feature.businesstype.api.dto.BusinessProductDTO;
import biz.craftline.server.feature.businesstype.api.request.AddNewBusinessProductRequest;
import biz.craftline.server.feature.businesstype.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusinessProductDTOMapper {

    @Lazy
    @Autowired
    BusinessTypeDTOMapper businessTypeDTOMapper;

    public BusinessProductDTO toDTO(BusinessProduct domain) {
        return BusinessProductDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .desc(domain.getDescription())
                .status(domain.getStatus())
                .businessId(domain.getBusinessId())
                .businessType(businessTypeDTOMapper.toDTO(domain.getBusinessType()))
                .categories(domain.getCategories())
                .amount(domain.getAmount() != null ? domain.getAmount() : 0f)
                .currency(domain.getCurrency())
                .brand(domain.getBrand())
                .thumbnailUrl(domain.getThumbnailUrl())
                .galleryUrls(domain.getGalleryUrls())
                .build();
    }

    public BusinessProduct toDomain(BusinessProductDTO dto) {
        return BusinessProduct.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDesc())
                .status(dto.getStatus())
                .businessId(dto.getBusinessId())
                .businessType(businessTypeDTOMapper.toDomain(dto.getBusinessType()))
                .categories(dto.getCategories())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .brand(dto.getBrand())
                .thumbnailUrl(dto.getThumbnailUrl())
                .galleryUrls(dto.getGalleryUrls())
                .build();
    }

    public BusinessProduct toDomain(AddNewBusinessProductRequest dto) {
        return BusinessProduct.builder()
                .name(dto.getName())
                .description(dto.getDesc())
                .businessId(dto.getBusinessId())
                .businessType(dto.getBusinessTypeId() != null
                        ? BusinessType.builder().id(dto.getBusinessTypeId()).build() : null)
                .brand(dto.getBrandId() != null ? Brand.builder().id(dto.getBrandId()).build() : null)
                .categories((dto.getCategories() != null ? dto.getCategories() : List.<Long>of()).stream()
                        .map(catId -> Category.builder().id(catId).build()).toList())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .status(dto.getStatus())
                .thumbnailUrl(dto.getThumbnailUrl())
                .galleryUrls(dto.getGalleryUrls())
                .build();
    }
}
