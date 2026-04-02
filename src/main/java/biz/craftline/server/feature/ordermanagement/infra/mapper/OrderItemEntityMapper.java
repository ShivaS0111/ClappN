package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;

public class OrderItemEntityMapper {
    public static OrderItemEntity toEntity(OrderItem model) {
        if (model == null) return null;
        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(model.getId());
        entity.setItemType(model.getItemType());
        entity.setItemId(model.getItemIId());
        entity.setQuantity(model.getQuantity());
        entity.setPrice(model.getPrice());
        // Item details & pricing
        entity.setItemName(model.getItemName());
        entity.setGstPercentage(model.getGstPercentage());
        entity.setGstAmount(model.getGstAmount());
        entity.setSgstPercentage(model.getSgstPercentage());
        entity.setCgstPercentage(model.getCgstPercentage());
        entity.setDiscountAmount(model.getDiscountAmount());
        entity.setDiscountType(model.getDiscountType());
        entity.setVirtualProductDetails(VirtualProductDetailsEntityMapper.toEntity(model.getVirtualProductDetails()));
        entity.setBookingDetails(BookingDetailsEntityMapper.toEntity(model.getBookingDetails()));
        entity.setDeliveryInfo(DeliveryInfoEntityMapper.toEntity(model.getDeliveryInfo()));

        return entity;
    }

    public static OrderItem toModel(OrderItemEntity entity) {
        if (entity == null) return null;
        OrderItem model = new OrderItem();
        model.setId(entity.getId());
        model.setItemType(entity.getItemType());
        model.setItemIId(entity.getItemId());
        model.setQuantity(entity.getQuantity());
        model.setPrice(entity.getPrice());
        // Item details & pricing
        model.setItemName(entity.getItemName());
        model.setGstPercentage(entity.getGstPercentage());
        model.setGstAmount(entity.getGstAmount());
        model.setSgstPercentage(entity.getSgstPercentage());
        model.setCgstPercentage(entity.getCgstPercentage());
        model.setDiscountAmount(entity.getDiscountAmount());
        model.setDiscountType(entity.getDiscountType());
        model.setVirtualProductDetails(VirtualProductDetailsEntityMapper.toModel(entity.getVirtualProductDetails()));
        model.setBookingDetails(BookingDetailsEntityMapper.toModel(entity.getBookingDetails()));
        model.setDeliveryInfo(DeliveryInfoEntityMapper.toModel(entity.getDeliveryInfo()));
        return model;
    }
}

