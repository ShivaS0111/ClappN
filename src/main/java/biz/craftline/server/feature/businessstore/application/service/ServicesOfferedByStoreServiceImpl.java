package biz.craftline.server.feature.businessstore.application.service;


import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreOfferedServiceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.util.UserUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ServicesOfferedByStoreServiceImpl implements ServicesOfferedByStoreService {

    @Autowired
    StoreOfferedServiceEntityMapper mapper;

    @Autowired
    ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;

    @Autowired
    BusinessServicesJpaRepository businessServicesJpaRepository;


    @Override
    public void deleteStoreServiceById(Long id) {
        servicesOfferedByStoreRepository.deleteStoreServiceById(id);
    }

    @Override
    public Optional<List<StoreOfferedService>> findServicesByStoreId(Long id) {
        Optional<List<StoreOfferedServiceEntity>> entities = servicesOfferedByStoreRepository.findServicesByStoreId(id);
        return Optional.of(entities.get().stream().map(mapper::toDomain).toList());
    }

    @Override
    public StoreOfferedService save(StoreOfferedService domain) {
        long userId = UserUtil.getCurrentUserId();
        StoreOfferedServiceEntity entity = mapper.toEntity(domain);
        entity.setCreatedBy(userId);
        StoreOfferedServiceEntity en = servicesOfferedByStoreRepository.save(entity);
        return mapper.toDomain(en);
    }

    @Override
    public List<StoreOfferedService> save(List<StoreOfferedService> domains) {
        long userId = UserUtil.getCurrentUserId();
        List<StoreOfferedServiceEntity> entities = domains.stream().map(domain -> {
                    StoreOfferedServiceEntity entity = mapper.toEntity(domain);
                    entity.setCreatedBy(userId);
                    return entity;
                }

        ).toList();
        List<StoreOfferedServiceEntity> en = servicesOfferedByStoreRepository.saveAll(entities);
        return en.stream().map(mapper::toDomain).toList();
    }

    @Override
    public StoreOfferedService findById(Long id) {
        Optional<StoreOfferedServiceEntity> service = servicesOfferedByStoreRepository.findById(id);
        service.orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        return mapper.toDomain(service.get());
    }

}