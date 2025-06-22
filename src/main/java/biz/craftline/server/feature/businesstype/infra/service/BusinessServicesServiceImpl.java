package biz.craftline.server.feature.businesstype.infra.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.service.BusinessServicesService;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.entity.mapper.BusinessServiceEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessTypeJpaRepository;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class BusinessServicesServiceImpl implements BusinessServicesService {


    @Autowired
    BusinessServicesJpaRepository repository;

    @Autowired
    BusinessTypeJpaRepository businessTypeJpaRepository;


    @Autowired
    BusinessServiceEntityMapper mapper;

    @Override
    public List<BusinessService> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteServiceById(Long id) {
        Optional<BusinessServiceEntity> bs = repository.findById(id);
        //bs.get().setStatus(2);
        repository.save(bs.get());
    }

    @Override
    public Optional<BusinessService> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public BusinessService save(BusinessService businessService) {
        BusinessServiceEntity entity = mapper.toEntity(businessService);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<BusinessService> save(List<BusinessService> businessServices) {
        List<BusinessServiceEntity> serviceEntities = businessServices
                .stream()
                .map(businessService -> mapper.toEntity(businessService)).toList();
        List<BusinessService> list = new ArrayList<>();
        for ( BusinessServiceEntity entity: serviceEntities ){
            BusinessServiceEntity savedEntity = repository.save(entity);
            list.add( mapper.toDomain(savedEntity));
        }
        return list;
    }


    @Override
    public List<BusinessService> findAllByBusinessTypeId(Long businessTypeId) {
        //return repository.findAllByBusinessTypeId(businessTypeId).stream().map(mapper::toDomain).toList();
        return repository.findAll().stream().map(mapper::toDomain).toList();

    }

    @Override
    public List<BusinessService> findByBusinessIdAndSearch(Long id, String keyword) {
        //return repository.searchByKeywordAndBusinessTypeId(keyword, id).stream().map(mapper::toDomain).toList();
        return repository.findAll().stream().map(mapper::toDomain).toList();

    }

    @Override
    public List<BusinessService> findBySearch(String keyword) {
        //return repository.searchByKeyword(keyword).stream().map(mapper::toDomain).toList();
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }
}

