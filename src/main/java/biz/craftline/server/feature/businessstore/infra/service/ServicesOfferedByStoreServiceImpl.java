package biz.craftline.server.feature.businessstore.infra.service;


import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.ServicesOfferedByStoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.entity.mapper.StoreOfferedServiceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
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
        BusinessServiceEntity businessServiceEntity = null;
        if(domain.getService().getId()!=null){
            businessServiceEntity = businessServicesJpaRepository.findById(domain.getService().getId() ).get();
        }
        StoreOfferedServiceEntity entity= mapper.toEntity(domain);
        //entity.setPrice( );
        entity.setService(businessServiceEntity);
        StoreOfferedServiceEntity en = servicesOfferedByStoreRepository.save(entity);
        return mapper.toDomain(en);
    }

}