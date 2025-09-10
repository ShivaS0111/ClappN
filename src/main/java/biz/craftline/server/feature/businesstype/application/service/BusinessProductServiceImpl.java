package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.service.BusinessProductsService;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessProductEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessTypeJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class BusinessProductServiceImpl implements BusinessProductsService {


    @Autowired
    BusinessProductJpaRepository repository;

    @Autowired
    BusinessTypeJpaRepository businessTypeJpaRepository;


    @Autowired
    BusinessProductEntityMapper mapper;

    @Override
    public List<BusinessProduct> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteServiceById(Long id) {
        Optional<BusinessProductEntity> bs = repository.findById(id);
        //bs.get().setStatus(2);
        repository.save(bs.get());
    }

    @Override
    public Optional<BusinessProduct> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public BusinessProduct save(BusinessProduct businessService) {
        BusinessProductEntity entity = mapper.toEntity(businessService);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<BusinessProduct> save(List<BusinessProduct> businessServices) {
        List<BusinessProductEntity> serviceEntities = businessServices
                .stream()
                .map(businessService -> mapper.toEntity(businessService)).toList();
        List<BusinessProduct> list = new ArrayList<>();
        for ( BusinessProductEntity entity: serviceEntities ){
            BusinessProductEntity savedEntity = repository.save(entity);
            list.add( mapper.toDomain(savedEntity));
        }
        return list;
    }



    @Override
    public List<BusinessProduct> findByBusinessIdAndSearch(Long id, String keyword) {
        //return repository.findByBusinessId(id).stream().map(mapper::toDomain).toList();
        //return repository.searchByKeywordAndBusinessTypeId(keyword, id).stream().map(mapper::toDomain).toList();
        return repository.searchByKeyword(keyword).stream().map(mapper::toDomain).toList();

    }

    @Override
    public List<BusinessProduct> findBySearch(String keyword) {
        //return repository.searchByKeyword(keyword).stream().map(mapper::toDomain).toList();
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }
}

