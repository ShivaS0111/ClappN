package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.DeliveryInfoDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.infra.entity.DeliveryInfoEntity;

public class DeliveryInfoDTOMapper {
    public static DeliveryInfoDTO toDTO(DeliveryInfo source) {
        if (source == null) return null;
        DeliveryInfoDTO target = new DeliveryInfoDTO();
        target.setAddress(source.getAddress());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setTrackingNumber(source.getTrackingNumber());
        target.setCourierService(source.getCourierService());
        target.setShippedDate(source.getShippedDate());
        return target;
    }

    public static DeliveryInfo fromDTO(DeliveryInfoDTO source) {
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
