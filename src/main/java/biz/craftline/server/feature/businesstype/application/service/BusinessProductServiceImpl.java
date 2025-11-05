package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessProductsService;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessProductEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessTypeJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
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
        return repository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteServiceById(Long id) {
        BusinessProductEntity bs = repository.findById(id)
                .orElseThrow(()->new RuntimeException("Business Product not found, id: %d".formatted(id)));
        repository.save(bs);
    }

    @Override
    public Optional<BusinessProduct> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public BusinessProduct save(BusinessProduct product) {
        BusinessProductEntity entity = getBusinessEntity(product);
        return mapper.toDomain(repository.save(entity));
    }

    private BusinessProductEntity getBusinessEntity(BusinessProduct product) {
        BusinessProductEntity entity = mapper.toEntity(product);
        if(product.getBusinessType()!=null && product.getBusinessType().getId()!=null){
            BusinessTypeEntity businessTypeEntity = businessTypeJpaRepository
                    .findById(product.getBusinessType().getId())
                    .orElseThrow(()-> new RuntimeException("BusinessType not valid")
            );
            entity.setBusinessType( businessTypeEntity);
        }
        return  entity;
    }

    @Override
    public List<BusinessProduct> save(List<BusinessProduct> products) {
        List<BusinessProductEntity> productEntities = products
                .stream()
                .map(this::getBusinessEntity)
                .toList();
        List<BusinessProduct> list = new ArrayList<>();
        for ( BusinessProductEntity entity: productEntities ){
            try {
                BusinessProductEntity savedEntity = repository.save(entity);
                list.add(mapper.toDomain(savedEntity));
            }catch (Exception e){
                log.error("Failed to save Product: {}", mapper.toDomain(entity));
            }
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

