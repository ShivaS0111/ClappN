package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.infra.entity.DeliveryInfoEntity;

public class DeliveryInfoEntityMapper {

    public static DeliveryInfoEntity toEntity(DeliveryInfo source) {
        if (source == null) return null;
        DeliveryInfoEntity target = new DeliveryInfoEntity();
        target.setAddress(source.getAddress());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setTrackingNumber(source.getTrackingNumber());
        target.setCourierService(source.getCourierService());
        target.setShippedDate(source.getShippedDate());
        return target;
    }

    public static DeliveryInfo toModel(DeliveryInfoEntity source) {
        if (source == null) return null;
        DeliveryInfo target = new DeliveryInfo();
        target.setAddress(source.getAddress());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setTrackingNumber(source.getTrackingNumber());
        target.setCourierService(source.getCourierService());
        target.setShippedDate(source.getShippedDate());
        return target;
    }
}

