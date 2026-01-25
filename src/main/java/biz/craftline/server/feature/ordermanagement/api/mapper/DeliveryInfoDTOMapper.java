package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.DeliveryInfoDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.infra.entity.DeliveryInfoEntity;
import biz.craftline.server.util.DateUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class DeliveryInfoDTOMapper {

    private final DateUtil dateUtil;

    public  DeliveryInfoDTO toDTO(DeliveryInfo source) {
        if (source == null) return null;
        DeliveryInfoDTO target = new DeliveryInfoDTO();
        target.setAddress(source.getAddress());
        target.setDeliveryDate(dateUtil.formatDateTime(source.getDeliveryDate()));
        target.setTrackingNumber(source.getTrackingNumber());
        target.setCourierService(source.getCourierService());
        target.setShippedDate(dateUtil.formatDateTime(source.getShippedDate()));
        return target;
    }

    public  DeliveryInfo fromDTO(DeliveryInfoDTO source) {
        if (source == null) return null;
        DeliveryInfo target = new DeliveryInfo();
        target.setAddress(source.getAddress());
        target.setDeliveryDate(dateUtil.parseLocalDateTime(source.getDeliveryDate()));
        target.setTrackingNumber(source.getTrackingNumber());
        target.setCourierService(source.getCourierService());
        target.setShippedDate(dateUtil.parseLocalDateTime(source.getShippedDate()));
        return target;
    }

}
