package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.enums.Status;
import biz.craftline.server.feature.businesstype.domain.model.Brand;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessProductsService;
import biz.craftline.server.feature.businesstype.infra.entity.BrandEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessProductEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BrandJpaRepository;
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
    BrandJpaRepository brandJpaRepository;


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
    public void deleteProductById(Long id) {
        BusinessProductEntity bs = repository.findById(id)
                .orElseThrow(()->new RuntimeException("Business Product not found, id: %d".formatted(id)));
        bs.setStatus(Status.DELETED.getCode());
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


    private BusinessTypeEntity getBusinessTypeEntity(BusinessType businessType) {
        if( businessType!=null && businessType.getId()!=null){
            return businessTypeJpaRepository
                    .findById(businessType.getId())
                    .orElseThrow(()-> new RuntimeException("BusinessType not valid"));
        }
        return  null;
    }

    private BrandEntity getBrandEntity(Brand brand) {
        if( brand!=null && brand.getId()!=null){
            return brandJpaRepository
                    .findById(brand.getId())
                    .orElseThrow(()-> new RuntimeException("Brand not valid"));
        }
        return  null;
    }

    private BusinessProductEntity getBusinessEntity(BusinessProduct product) {
        BusinessProductEntity entity = mapper.toEntity(product);
        entity.setBusinessType( getBusinessTypeEntity(product.getBusinessType() ));
        entity.setBrand( getBrandEntity(product.getBrand() ));
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
    public List<BusinessProduct> findByBusinessTypeIdAndSearch(Long businessTypeId, String keyword) {
        //return repository.findByBusinessId(id).stream().map(mapper::toDomain).toList();
        //return repository.searchByKeywordAndBusinessTypeId(keyword, id).stream().map(mapper::toDomain).toList();
        return repository.searchByKeyword(keyword).stream().map(mapper::toDomain).toList();

    }

    @Override
    public List<BusinessProduct> findBySearch(String keyword) {
        //return repository.searchByKeyword(keyword).stream().map(mapper::toDomain).toList();
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<BusinessProduct> findByBusinessTypeId(Long businessTypeId) {
        return repository.findByBusinessType_Id(businessTypeId).stream().map(mapper::toDomain).toList();
    }
}

