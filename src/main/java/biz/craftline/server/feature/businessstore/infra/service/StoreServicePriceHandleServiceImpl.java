package biz.craftline.server.feature.businessstore.infra.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreServicePrice;
import biz.craftline.server.feature.businessstore.domain.service.StoreServicePriceHandleService;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.StoreServicePriceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.StoreServicePriceHandleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@AllArgsConstructor
@Repository
public class StoreServicePriceHandleServiceImpl implements StoreServicePriceHandleService {

    @Autowired
    StoreServicePriceEntityMapper mapper;

    @Autowired
    StoreServicePriceHandleRepository repository;


    @Override
    public StoreServicePrice save(StoreServicePrice entity) {
         return mapper.toDomain( repository.save(mapper.toEntity(entity)) );
    }

    @Override
    public List<StoreServicePrice> findAll(Long serviceId) {
        return repository.findAllByServiceId(serviceId).parallelStream().map(mapper::toDomain).toList();
    }
}
