package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between BusinessEntity and Business domain model.
 */
@Component
public class BusinessEntityMapper {
    private final StoreEntityMapper storeEntityMapper;

    public BusinessEntityMapper(@Lazy StoreEntityMapper storeEntityMapper) {
        this.storeEntityMapper = storeEntityMapper;
    }

    /**
     * Converts a BusinessEntity to a Business domain model.
     * @param entity the BusinessEntity
     * @return the Business domain model
     */
    public Business toDomain(BusinessEntity entity) {

        Set<Store> stores = null;
        /*if (entity.getStores() != null) {
            stores = entity.getStores()
                    .stream()
                    .map(storeEntityMapper::toDomain)
                    .collect(Collectors.toSet());
        }*/
        return Business.builder()
                .id(entity.getId())
                .businessName(entity.getBusinessName())
                .description(entity.getDescription())
                //.stores(new HashSet<>(stores))
                .build();
    }

    /**
     * Converts a Business domain model to a BusinessEntity.
     * @param business the Business domain model
     * @return the BusinessEntity
     */
    public BusinessEntity toEntity(Business business) {
        return BusinessEntity.builder()
                .businessName(business.getBusinessName())
                .description(business.getDescription())
                .build();
    }
}
