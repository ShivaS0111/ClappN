package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.infra.entity.StoreProductEntity;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class StoreProductEntityMapper {

    public StoreOfferedProduct toDomain(StoreProductEntity entity) {
        if (entity == null) {
            return null;
        }

        return StoreOfferedProduct.builder()
                .id(entity.getId())
                .aliasName(entity.getAliasName())
                .description(entity.getDescription())
                .storeId(entity.getStoreId())
                .status(entity.getStatus())
                .businessProductId(entity.getBusinessProductId())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toLocalDateTime() : null)
                .build();
    }

    public StoreProductEntity toEntity(StoreOfferedProduct domain) {
        if (domain == null) {
            return null;
        }

        return StoreProductEntity.builder()
                .id(domain.getId())
                .aliasName(domain.getAliasName())
                .description(domain.getDescription())
                .storeId(domain.getStoreId())
                .status(domain.getStatus())
                .businessProductId(domain.getBusinessProductId())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt() != null ? Timestamp.valueOf(domain.getCreatedAt()) : null)
                .updatedAt(domain.getUpdatedAt() != null ? Timestamp.valueOf(domain.getUpdatedAt()) : null)
                .build();
    }
}
