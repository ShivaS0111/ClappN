package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.infra.entity.DeliveryInfoEntity;

public class DeliveryInfoEntityMapper {
    public static DeliveryInfoEntity toEntity(DeliveryInfo model) {
        if (model == null) return null;
        DeliveryInfoEntity entity = new DeliveryInfoEntity();
        entity.setAddress(model.getAddress());
        entity.setDeliveryDate(model.getDeliveryDate());
        return entity;
    }

    public static DeliveryInfo toModel(DeliveryInfoEntity entity) {
        if (entity == null) return null;
        DeliveryInfo model = new DeliveryInfo();
        model.setAddress(entity.getAddress());
        model.setDeliveryDate(entity.getDeliveryDate());
        return model;
    }
}

