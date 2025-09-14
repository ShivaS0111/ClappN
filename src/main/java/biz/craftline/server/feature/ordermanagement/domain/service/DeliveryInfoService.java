package biz.craftline.server.feature.ordermanagement.domain.service;

import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import java.util.List;

public interface DeliveryInfoService {
    List<DeliveryInfo> getAllDeliveryInfo();
    DeliveryInfo getDeliveryInfo(Long id);
    DeliveryInfo addDeliveryInfo(DeliveryInfo deliveryInfo);
    DeliveryInfo updateDeliveryInfo(Long id, DeliveryInfo deliveryInfo);
    void deleteDeliveryInfo(Long id);
}

