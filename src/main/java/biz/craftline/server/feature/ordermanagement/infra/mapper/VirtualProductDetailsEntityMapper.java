package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.VirtualProductDetails;
import biz.craftline.server.feature.ordermanagement.infra.entity.VirtualProductDetailsEntity;

public class VirtualProductDetailsEntityMapper {
    public static VirtualProductDetailsEntity toEntity(VirtualProductDetails model) {
        if (model == null) return null;
        VirtualProductDetailsEntity entity = new VirtualProductDetailsEntity();
        entity.setLicenseKey(model.getLicenseKey());
        entity.setActivationDate(model.getActivationDate());
        return entity;
    }

    public static VirtualProductDetails toModel(VirtualProductDetailsEntity entity) {
        if (entity == null) return null;
        VirtualProductDetails model = new VirtualProductDetails();
        model.setLicenseKey(entity.getLicenseKey());
        model.setActivationDate(entity.getActivationDate());
        return model;
    }
}

