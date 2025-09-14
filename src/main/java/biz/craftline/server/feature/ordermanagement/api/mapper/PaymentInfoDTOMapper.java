package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.PaymentInfoDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.PaymentInfo;

public class PaymentInfoDTOMapper {
    public static PaymentInfoDTO toDTO(PaymentInfo entity) {
        if (entity == null) return null;
        PaymentInfoDTO dto = new PaymentInfoDTO();
        dto.setId(entity.getId());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setTransactionId(entity.getTransactionId());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public static PaymentInfo fromDTO(PaymentInfoDTO dto) {
        if (dto == null) return null;
        PaymentInfo entity = new PaymentInfo();
        entity.setId(dto.getId());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setPaymentStatus(dto.getPaymentStatus());
        entity.setTransactionId(dto.getTransactionId());
        entity.setAmount(dto.getAmount());
        entity.setStatus(dto.getStatus());
        return entity;
    }
}
