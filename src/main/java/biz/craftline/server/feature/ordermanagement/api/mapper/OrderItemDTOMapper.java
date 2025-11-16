package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.OrderItemDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;

public class OrderItemDTOMapper {
    public static OrderItemDTO toDTO(OrderItem entity) {
        if (entity == null) return null;
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(entity.getId());
        dto.setItemType(entity.getItemType());
        dto.setItemId(entity.getItemIId());
        dto.setQuantity(entity.getQuantity());
        dto.setPrice(entity.getPrice());
        dto.setDeliveryInfo(DeliveryInfoDTOMapper.toDTO(entity.getDeliveryInfo()));
        // Add mapping for nested objects if needed
        return dto;
    }

    public static OrderItem fromDTO(OrderItemDTO dto) {
        if (dto == null) return null;
        OrderItem entity = new OrderItem();
        entity.setId(dto.getId());
        entity.setItemType(dto.getItemType());
        entity.setItemIId(dto.getItemId());
        entity.setQuantity(dto.getQuantity());
        entity.setPrice(dto.getPrice());
        entity.setDeliveryInfo(DeliveryInfoDTOMapper.fromDTO(dto.getDeliveryInfo()));
        // Add mapping for nested objects if needed
        return entity;
    }
}

