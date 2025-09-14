package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import java.util.stream.Collectors;

public class OrderEntityMapper {
    public static OrderEntity toEntity(Order model) {
        if (model == null) return null;
        OrderEntity entity = new OrderEntity();
        entity.setId(model.getId());
        entity.setCustomerId(model.getCustomerId());
        entity.setOrderDate(model.getOrderDate());
        entity.setStatus(model.getStatus());
        entity.setItems(model.getItems() != null ? model.getItems().stream().map(OrderItemEntityMapper::toEntity).collect(Collectors.toList()) : null);
        entity.setDeliveryInfo(DeliveryInfoEntityMapper.toEntity(model.getDeliveryInfo()));
        entity.setPaymentInfo(PaymentInfoEntityMapper.toEntity(model.getPaymentInfo()));
        return entity;
    }

    public static Order toModel(OrderEntity entity) {
        if (entity == null) return null;
        Order model = new Order();
        model.setId(entity.getId());
        model.setCustomerId(entity.getCustomerId());
        model.setOrderDate(entity.getOrderDate());
        model.setStatus(entity.getStatus());
        model.setItems(entity.getItems() != null ? entity.getItems().stream().map(OrderItemEntityMapper::toModel).collect(Collectors.toList()) : null);
        model.setDeliveryInfo(DeliveryInfoEntityMapper.toModel(entity.getDeliveryInfo()));
        model.setPaymentInfo(PaymentInfoEntityMapper.toModel(entity.getPaymentInfo()));
        return model;
    }
}

