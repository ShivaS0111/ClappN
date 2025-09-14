package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.OrderItemDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;

public class OrderItemDTOMapper {
    public static OrderItemDTO toDTO(OrderItem entity) {
        if (entity == null) return null;
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setProductId(entity.getProductId());
        dto.setServiceId(entity.getServiceId());
        dto.setQuantity(entity.getQuantity());
        dto.setPrice(entity.getPrice());
        // Add mapping for nested objects if needed
        return dto;
    }

    public static OrderItem fromDTO(OrderItemDTO dto) {
        if (dto == null) return null;
        OrderItem entity = new OrderItem();
        entity.setId(dto.getId());
        entity.setType(dto.getType());
        entity.setProductId(dto.getProductId());
        entity.setServiceId(dto.getServiceId());
        entity.setQuantity(dto.getQuantity());
        entity.setPrice(dto.getPrice());
        // Add mapping for nested objects if needed
        return entity;
    }
}

