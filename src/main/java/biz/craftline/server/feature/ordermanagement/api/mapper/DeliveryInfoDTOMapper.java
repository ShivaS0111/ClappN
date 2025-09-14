package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.DeliveryInfoDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;

public class DeliveryInfoDTOMapper {
    public static DeliveryInfoDTO toDTO(DeliveryInfo entity) {
        if (entity == null) return null;
        DeliveryInfoDTO dto = new DeliveryInfoDTO();
        dto.setDeliveryType(entity.getDeliveryType());
        dto.setAddress(entity.getAddress());
        dto.setDeliveryStatus(entity.getDeliveryStatus());
        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setDeliveryDate(entity.getDeliveryDate());
        return dto;
    }

    public static DeliveryInfo fromDTO(DeliveryInfoDTO dto) {
        if (dto == null) return null;
        DeliveryInfo entity = new DeliveryInfo();
        entity.setDeliveryType(dto.getDeliveryType());
        entity.setAddress(dto.getAddress());
        entity.setDeliveryStatus(dto.getDeliveryStatus());
        entity.setTrackingNumber(dto.getTrackingNumber());
        entity.setDeliveryDate(dto.getDeliveryDate());
        return entity;
    }
}
