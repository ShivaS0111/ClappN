package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessServiceEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class StoreOfferedServiceEntityMapper {

    public StoreOfferedService toDomain(StoreOfferedServiceEntity entity){
        StoreOfferedService service = new StoreOfferedService();
        service.setId(entity.getId());
        service.setAliasName(entity.getAliasName());
        service.setDescription(entity.getDescription());
        service.setStatus(entity.getStatus());
        service.setStoreId(entity.getStoreId());
        service.setBusinessId(entity.getBusinessId());
        service.setBusinessServiceId(entity.getBusinessServiceId());
        service.setCreatedBy(entity.getCreatedBy());
        service.setThumbnailUrl(entity.getThumbnailUrl());
        service.setGalleryUrls(entity.getGalleryUrls());
        return service;
    }

    public StoreOfferedServiceEntity toEntity(StoreOfferedService store){
        StoreOfferedServiceEntity entity = new StoreOfferedServiceEntity();
        entity.setId(store.getId());
        entity.setAliasName(store.getAliasName());
        entity.setDescription(store.getDescription());
        entity.setStoreId(store.getStoreId());
        entity.setStatus(store.getStatus());
        entity.setBusinessId(store.getBusinessId());
        entity.setBusinessServiceId(store.getBusinessServiceId());
        entity.setCreatedBy(store.getCreatedBy());
        entity.setThumbnailUrl(store.getThumbnailUrl());
        entity.setGalleryUrls(store.getGalleryUrls());
        return entity;
    }
}
