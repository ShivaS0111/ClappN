package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.BusinessEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.BusinessEntityJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class BusinessEntityServiceImpl implements BusinessEntityService {

    @Autowired
    BusinessEntityMapper mapper;

    @Autowired
    BusinessEntityJpaRepository businessEntityRepository;


    @Override
    public List<Business> findAll() {
        return businessEntityRepository.findAll().stream().map(mapper::toDomain).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void deleteBusinessTypeById(Long id) {
        businessEntityRepository.deleteBusinessTypeById(id);
    }

    @Override
    public Optional<Business> findById(Long id) {
        return businessEntityRepository.findById(id).map(businessEntity -> mapper.toDomain(businessEntity));
    }

    @Override
    public Business save(Business business) {
        BusinessEntity entity = mapper.toEntity(business);
        return mapper.toDomain(businessEntityRepository.save(entity));
    }

    @Override
    public List<Business> search(String keyword) {
        return businessEntityRepository.findByNameContaining(keyword.toLowerCase()).stream().map(mapper::toDomain).collect(java.util.stream.Collectors.toList());
    }
}
