package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;

public class OrderItemEntityMapper {
    public static OrderItemEntity toEntity(OrderItem model) {
        if (model == null) return null;
        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(model.getId());
        entity.setType(model.getType());
        entity.setProductId(model.getProductId());
        entity.setServiceId(model.getServiceId());
        entity.setQuantity(model.getQuantity());
        entity.setPrice(model.getPrice());
        entity.setVirtualProductDetails(VirtualProductDetailsEntityMapper.toEntity(model.getVirtualProductDetails()));
        entity.setBookingDetails(BookingDetailsEntityMapper.toEntity(model.getBookingDetails()));
        entity.setDeliveryInfo(DeliveryInfoEntityMapper.toEntity(model.getDeliveryInfo()));

        return entity;
    }

    public static OrderItem toModel(OrderItemEntity entity) {
        if (entity == null) return null;
        OrderItem model = new OrderItem();
        model.setId(entity.getId());
        model.setType(entity.getType());
        model.setProductId(entity.getProductId());
        model.setServiceId(entity.getServiceId());
        model.setQuantity(entity.getQuantity());
        model.setPrice(entity.getPrice());
        model.setVirtualProductDetails(VirtualProductDetailsEntityMapper.toModel(entity.getVirtualProductDetails()));
        model.setBookingDetails(BookingDetailsEntityMapper.toModel(entity.getBookingDetails()));
        model.setDeliveryInfo(DeliveryInfoEntityMapper.toModel(entity.getDeliveryInfo()));
        return model;
    }
}

