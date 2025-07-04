package biz.craftline.server.feature.businesstype.infra.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessTypeService;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.entity.mapper.BusinessTypeEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessTypeJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class BusinessTypeServiceImpl implements BusinessTypeService {

    @Autowired
    BusinessTypeJpaRepository repository;

    @Autowired
    BusinessTypeEntityMapper mapper;


    @Override
    public List<BusinessType> findAll() {
        return repository.findAll().parallelStream().map( mapper::toDomain).toList();
    }

    @Override
    public void deleteBusinessTypeById(Long id) {
        Optional<BusinessTypeEntity> typeOp = repository.findById(id);
        if (typeOp.isPresent()) {
            BusinessTypeEntity type = typeOp.get();
            //type.setStatus(2);
            repository.save(type);
        }

    }

    @Override
    public Optional<BusinessType> findById(Long id) {
        Optional<BusinessTypeEntity> entity = repository.findById(id);
        return entity.map(businessTypeEntity -> mapper.toDomain(businessTypeEntity));
    }

    @Override
    public BusinessType save(BusinessType businessType) {
        System.out.println("==>BusinessType Save: " + businessType);
        BusinessTypeEntity entity = repository.save(mapper.toEntity(businessType));
        return mapper.toDomain(entity);
    }

    @Override
    public List<BusinessType> findAllByIds(List<Long> businessTypeId) {
        return repository.findAllById( businessTypeId ).stream().map( mapper::toDomain).toList();
    }
}

