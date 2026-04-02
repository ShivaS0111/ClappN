package biz.craftline.server.feature.ordermanagement.infra.mapper;

import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;
import biz.craftline.server.feature.paymentmanagement.infra.mapper.PaymentInfoEntityMapper;

import java.util.List;
import java.util.stream.Collectors;

public class OrderEntityMapper {
    public static OrderEntity toEntity(Order model) {
        if (model == null) return null;
        OrderEntity entity = new OrderEntity();
        entity.setId(model.getId());
        entity.setStoreId(model.getStoreId());
        entity.setCustomerId(model.getCustomerId());
        entity.setTotalAmount(model.getTotalAmount() != null ? java.math.BigDecimal.valueOf(model.getTotalAmount()) : null);
        entity.setOrderDate(model.getOrderDate());
        entity.setStatus(model.getStatus());
        // Pricing breakdown
        entity.setSubtotal(model.getSubtotal() != null ? java.math.BigDecimal.valueOf(model.getSubtotal()) : null);
        entity.setTotalGst(model.getTotalGst() != null ? java.math.BigDecimal.valueOf(model.getTotalGst()) : null);
        entity.setTotalDiscount(model.getTotalDiscount() != null ? java.math.BigDecimal.valueOf(model.getTotalDiscount()) : null);
        entity.setBillDiscount(model.getBillDiscount() != null ? java.math.BigDecimal.valueOf(model.getBillDiscount()) : null);
        entity.setBillDiscountType(model.getBillDiscountType());
        entity.setCouponCode(model.getCouponCode());
        entity.setNotes(model.getNotes());
        entity.setItems(model.getItems() != null ? model.getItems().stream().map(OrderItemEntityMapper::toEntity).collect(Collectors.toList()) : null);
        entity.setDeliveryInfo(DeliveryInfoEntityMapper.toEntity(model.getDeliveryInfo()));
        entity.setPaymentInfo(PaymentInfoEntityMapper.toEntity(model.getPaymentInfo()));
        return entity;
    }

    public static Order toModel(OrderEntity entity) {
        if (entity == null) return null;
        Order model = new Order();
        model.setId(entity.getId());
        model.setStoreId(entity.getStoreId());
        model.setCustomerId(entity.getCustomerId());
        model.setTotalAmount(entity.getTotalAmount() != null ? entity.getTotalAmount().doubleValue() : null);
        model.setOrderDate(entity.getOrderDate());
        model.setStatus(entity.getStatus());
        // Pricing breakdown
        model.setSubtotal(entity.getSubtotal() != null ? entity.getSubtotal().doubleValue() : null);
        model.setTotalGst(entity.getTotalGst() != null ? entity.getTotalGst().doubleValue() : null);
        model.setTotalDiscount(entity.getTotalDiscount() != null ? entity.getTotalDiscount().doubleValue() : null);
        model.setBillDiscount(entity.getBillDiscount() != null ? entity.getBillDiscount().doubleValue() : null);
        model.setBillDiscountType(entity.getBillDiscountType());
        model.setCouponCode(entity.getCouponCode());
        model.setNotes(entity.getNotes());
        model.setItems(entity.getItems() != null ? entity.getItems().stream().map(OrderItemEntityMapper::toModel).collect(Collectors.toList()) : null);
        model.setDeliveryInfo(DeliveryInfoEntityMapper.toModel(entity.getDeliveryInfo()));
        model.setPaymentInfo(PaymentInfoEntityMapper.toModel(entity.getPaymentInfo()));
        return model;
    }

    public static Order toDomain(OrderEntity e, List<OrderItemEntity> items) {
        if (e == null) return null;
        return Order.builder()
                .id(e.getId())
                .storeId(e.getStoreId())
                .customerId(e.getCustomerId())
                .status(e.getStatus())
                .totalAmount(e.getTotalAmount().doubleValue())
                .orderDate(e.getOrderDate())
                .subtotal(e.getSubtotal() != null ? e.getSubtotal().doubleValue() : null)
                .totalGst(e.getTotalGst() != null ? e.getTotalGst().doubleValue() : null)
                .totalDiscount(e.getTotalDiscount() != null ? e.getTotalDiscount().doubleValue() : null)
                .billDiscount(e.getBillDiscount() != null ? e.getBillDiscount().doubleValue() : null)
                .billDiscountType(e.getBillDiscountType())
                .couponCode(e.getCouponCode())
                .notes(e.getNotes())
                .items(items.stream().map(it -> OrderItem.builder()
                        .id(it.getId())
                        .itemType(it.getItemType())
                        .itemIId(it.getItemId())
                        .quantity(it.getQuantity())
                        .price(it.getPrice())
                        .itemName(it.getItemName())
                        .gstPercentage(it.getGstPercentage())
                        .gstAmount(it.getGstAmount())
                        .sgstPercentage(it.getSgstPercentage())
                        .cgstPercentage(it.getCgstPercentage())
                        .discountAmount(it.getDiscountAmount())
                        .discountType(it.getDiscountType())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}

