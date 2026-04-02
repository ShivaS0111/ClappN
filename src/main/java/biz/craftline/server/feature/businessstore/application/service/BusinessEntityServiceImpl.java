package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
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
import java.util.stream.Collectors;

@AllArgsConstructor
@Repository
public class BusinessEntityServiceImpl implements BusinessEntityService {

    @Autowired
    BusinessEntityMapper mapper;

    @Autowired
    BusinessEntityJpaRepository businessEntityRepository;

    @Autowired
    SecurityContextService securityContextService;

    @Override
    public List<Business> findAll() {
        List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();
        if (accessibleBusinessIds == null) {
            // SYSTEM_ADMIN — unrestricted
            return businessEntityRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
        }
        if (accessibleBusinessIds.isEmpty()) {
            return List.of();
        }
        return businessEntityRepository.findAllById(accessibleBusinessIds).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteBusinessTypeById(Long id) {
        securityContextService.validateBusinessAccess(id);
        businessEntityRepository.deleteBusinessTypeById(id);
    }

    @Override
    public Optional<Business> findById(Long id) {
        securityContextService.validateBusinessAccess(id);
        return businessEntityRepository.findById(id).map(businessEntity -> mapper.toDomain(businessEntity));
    }

    @Override
    public Business save(Business business) {
        if (business.getId() != null) {
            securityContextService.validateBusinessAccess(business.getId());
        }
        BusinessEntity entity = mapper.toEntity(business);
        return mapper.toDomain(businessEntityRepository.save(entity));
    }

    @Override
    public List<Business> search(String keyword) {
        List<Business> results = businessEntityRepository.findByNameContaining(keyword.toLowerCase())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
        List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();
        if (accessibleBusinessIds == null) {
            return results; // SYSTEM_ADMIN
        }
        return results.stream()
                .filter(b -> accessibleBusinessIds.contains(b.getId()))
                .collect(Collectors.toList());
    }
}
