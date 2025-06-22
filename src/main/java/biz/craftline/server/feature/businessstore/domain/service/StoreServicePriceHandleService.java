package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreServicePrice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StoreServicePriceHandleService {

    StoreServicePrice save(StoreServicePrice entity);

    List<StoreServicePrice> findAll(Long serviceId);
}
