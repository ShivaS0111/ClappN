package biz.craftline.server.feature.paymentmanagement.infra.mapper;

import biz.craftline.server.feature.paymentmanagement.domain.model.PaymentInfo;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentInfoEntity;

public class PaymentInfoEntityMapper {
    public static PaymentInfoEntity toEntity(PaymentInfo model) {
        if (model == null) return null;
        PaymentInfoEntity entity = new PaymentInfoEntity();
        entity.setPaymentMethod(model.getPaymentMethod());
        entity.setAmount(model.getAmount());
        entity.setStatus(model.getStatus());
        return entity;
    }

    public static PaymentInfo toModel(PaymentInfoEntity entity) {
        if (entity == null) return null;
        PaymentInfo model = new PaymentInfo();
        model.setPaymentMethod(entity.getPaymentMethod());
        model.setAmount(entity.getAmount());
        model.setStatus(entity.getStatus());
        return model;
    }
}

