package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.enums.Status;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.BusinessServicesService;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessServiceEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessTypeJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.CategoryJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class BusinessServicesServiceImpl implements BusinessServicesService {


    @Autowired
    BusinessServicesJpaRepository repository;

    @Autowired
    BusinessTypeJpaRepository businessTypeJpaRepository;


    @Autowired
    BusinessServiceEntityMapper mapper;

    @Autowired
    CategoryJpaRepository categoryJpaRepository;

    @Autowired
    CatalogBusinessOwnership catalogOwnership;

    @Override
    public List<BusinessService> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .filter(s -> catalogOwnership.isVisibleToCaller(s.getBusinessId()))
                .toList();
    }

    @Override
    public void deleteServiceById(Long id) {
        BusinessServiceEntity bs = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business Service not found, id: %d".formatted(id)));
        catalogOwnership.assertCanMutate(bs.getBusinessId());
        bs.setStatus(Status.DELETED.getCode());
        repository.save(bs);
    }

    @Override
    public Optional<BusinessService> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain).map(s -> {
            catalogOwnership.assertCanRead(s.getBusinessId());
            return s;
        });
    }

    @Override
    public BusinessService save(BusinessService businessService) {
        businessService.setBusinessId(
                catalogOwnership.resolveBusinessIdForCreate(businessService.getBusinessId()));
        assertUniqueServiceName(businessService.getBusinessId(), businessService.getServiceName(), null);
        BusinessServiceEntity entity = getBusinessServiceEntity(businessService);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<BusinessService> save(List<BusinessService> businessServices) {
        return businessServices.stream().map(this::save).toList();
    }


    private BusinessServiceEntity getBusinessServiceEntity(BusinessService service) {
        BusinessServiceEntity entity = mapper.toEntity(service);
        entity.setBusinessId(service.getBusinessId());

        List<CategoryEntity> categoryList = getCategoryList(service.getCategories());
        entity.setCategories(categoryList != null ? categoryList : new ArrayList<>());

        entity.setBusinessType(getBusinessTypeEntity(service.getBusinessType()));
        return entity;
    }

    private List<CategoryEntity> getCategoryList(List<Category> categories) {
        if (categories != null && !categories.isEmpty()) {
            List<Long> catIds = categories.stream()
                    .map(Category::getId)
                    .filter(Objects::nonNull)
                    .toList();
            return categoryJpaRepository.findAllByIdIn(new HashSet<>(catIds));
        }
        return null;
    }

    private BusinessTypeEntity getBusinessTypeEntity(BusinessType businessType) {
        if (businessType == null || businessType.getId() == null) {
            throw new IllegalArgumentException("businessTypeId is required");
        }
        if (businessType.getId() <= 0) {
            throw new IllegalArgumentException("businessTypeId is required");
        }
        return businessTypeJpaRepository
                .findById(businessType.getId())
                .orElseThrow(() -> new EntityNotFoundException("BusinessType not valid"));
    }

    @Override
    public List<BusinessService> searchByKeywordAndBusinessType(Long businessTypeId, String keyword) {
        return repository.searchByKeywordAndBusinessType(keyword, businessTypeId)
                .stream()
                .map(mapper::toDomain)
                .filter(s -> catalogOwnership.isVisibleToCaller(s.getBusinessId()))
                .toList();
    }

    @Override
    public List<BusinessService> findBySearch(String keyword) {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .filter(s -> catalogOwnership.isVisibleToCaller(s.getBusinessId()))
                .toList();
    }

    @Override
    public List<BusinessService> findByBusinessTypeId(Long businessTypeId) {
        return repository.findByBusinessType_Id(businessTypeId).stream()
                .map(mapper::toDomain)
                .filter(s -> catalogOwnership.isVisibleToCaller(s.getBusinessId()))
                .toList();
    }

    @Override
    public List<BusinessService> findByBusinessId(Long businessId) {
        catalogOwnership.assertCanRead(businessId);
        return repository.findByBusinessId(businessId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public BusinessService update(BusinessService businessService) {
        BusinessServiceEntity service = repository.findById(businessService.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Business Service ID: " + businessService.getId()));
        catalogOwnership.assertCanMutate(service.getBusinessId());

        if (businessService.getServiceName() != null) {
            assertUniqueServiceName(service.getBusinessId(), businessService.getServiceName(), service.getId());
            service.setServiceName(businessService.getServiceName());
        }

        if (businessService.getDescription() != null)
            service.setDescription(businessService.getDescription());

        service.setStatus(businessService.getStatus());

        if (businessService.getAmount() != null)
            service.setAmount(businessService.getAmount());

        if (businessService.getCurrency() != null)
            service.setCurrency(businessService.getCurrency());

        if (businessService.getBusinessType() != null &&
                businessService.getBusinessType().getId() != null &&
                (service.getBusinessType() == null ||
                        !businessService.getBusinessType().getId()
                                .equals(service.getBusinessType().getId()))) {

            service.setBusinessType(
                    getBusinessTypeEntity(businessService.getBusinessType())
            );
        }

        if (businessService.getCategories() != null &&
                !businessService.getCategories().isEmpty()) {

            Set<Long> newCategoryIds = businessService.getCategories().stream()
                    .map(Category::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<Long> existingCategoryIds = service.getCategories().stream()
                    .map(CategoryEntity::getId)
                    .collect(Collectors.toSet());

            if (!existingCategoryIds.equals(newCategoryIds)) {
                service.setCategories(
                        categoryJpaRepository.findAllByIdIn(newCategoryIds)
                );
            }
        } else {
            service.setCategories(new ArrayList<>());
        }

        return mapper.toDomain(repository.save(service));
    }

    private void assertUniqueServiceName(Long businessId, String name, Long excludeId) {
        boolean duplicate;
        if (excludeId == null) {
            duplicate = businessId == null
                    ? repository.existsByBusinessIdIsNullAndServiceNameIgnoreCase(name)
                    : repository.existsByBusinessIdAndServiceNameIgnoreCase(businessId, name);
        } else {
            duplicate = businessId == null
                    ? repository.existsByBusinessIdIsNullAndServiceNameIgnoreCaseAndIdNot(name, excludeId)
                    : repository.existsByBusinessIdAndServiceNameIgnoreCaseAndIdNot(businessId, name, excludeId);
        }
        catalogOwnership.assertUniqueName("Service", name, duplicate);
    }
}

