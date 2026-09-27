package biz.craftline.server.feature.businesstype.infra.mapper;

import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusinessProductEntityMapper {

    @Autowired
    CategoryEntityMapper mapper;

    @Autowired
    BusinessTypeEntityMapper businessTypeEntityMapper;

    @Lazy
    @Autowired
    BrandEntityMapper brandEntityMapper;

    public BusinessProductEntity toEntity(BusinessProduct domain) {
        BusinessProductEntity entity = new BusinessProductEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setBusinessId(domain.getBusinessId());
        entity.setAmount(domain.getAmount());
        entity.setCategories(domain.getCategories() != null
                ? domain.getCategories().stream().map(mapper::toEntity).toList()
                : List.of());
        entity.setCurrency(domain.getCurrency());
        entity.setThumbnailUrl(domain.getThumbnailUrl());
        entity.setGalleryUrls(domain.getGalleryUrls());
        return entity;
    }

    public BusinessProduct toDomain(BusinessProductEntity entity) {
        return BusinessProduct.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .businessId(entity.getBusinessId())
                .businessType(entity.getBusinessType() != null
                        ? businessTypeEntityMapper.toDomain(entity.getBusinessType()) : null)
                .categories(entity.getCategories() != null
                        ? entity.getCategories().stream().map(mapper::toDomain).toList()
                        : List.of())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .brand(entity.getBrand() != null ? brandEntityMapper.toDomain(entity.getBrand()) : null)
                .thumbnailUrl(entity.getThumbnailUrl())
                .galleryUrls(entity.getGalleryUrls())
                .build();
    }
}
