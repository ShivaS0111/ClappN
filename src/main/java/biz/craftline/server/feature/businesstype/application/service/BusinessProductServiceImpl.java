package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.enums.Status;
import biz.craftline.server.feature.businesstype.domain.model.Brand;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.domain.service.BusinessProductsService;
import biz.craftline.server.feature.businesstype.infra.entity.BrandEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessProductEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BrandJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessTypeJpaRepository;
import biz.craftline.server.feature.businesstype.infra.repository.CategoryJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    CategoryJpaRepository categoryJpaRepository;


    @Autowired
    BusinessProductEntityMapper mapper;

    @Autowired
    CatalogBusinessOwnership catalogOwnership;

    @Override
    public List<BusinessProduct> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDomain)
                .filter(p -> catalogOwnership.isVisibleToCaller(p.getBusinessId()))
                .toList();
    }

    @Override
    public void deleteProductById(Long id) {
        BusinessProductEntity bs = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business Product not found, id: %d".formatted(id)));
        catalogOwnership.assertCanMutate(bs.getBusinessId());
        bs.setStatus(Status.DELETED.getCode());
        repository.save(bs);
    }

    @Override
    public Optional<BusinessProduct> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain).map(p -> {
            catalogOwnership.assertCanRead(p.getBusinessId());
            return p;
        });
    }

    @Override
    public BusinessProduct save(BusinessProduct product) {
        product.setBusinessId(catalogOwnership.resolveBusinessIdForCreate(product.getBusinessId()));
        assertUniqueProductName(product.getBusinessId(), product.getName(), null);
        BusinessProductEntity entity = getBusinessEntity(product);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public BusinessProduct update(BusinessProduct businessProduct) {

        BusinessProductEntity product = repository.findById(businessProduct.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Business Product not found, id: " + businessProduct.getId()
                ));
        catalogOwnership.assertCanMutate(product.getBusinessId());

        if (businessProduct.getName() != null) {
            assertUniqueProductName(product.getBusinessId(), businessProduct.getName(), product.getId());
            product.setName(businessProduct.getName());
        }

        if (businessProduct.getDescription() != null)
            product.setDescription(businessProduct.getDescription());

        product.setStatus(businessProduct.getStatus());

        if (businessProduct.getAmount() != null)
            product.setAmount(businessProduct.getAmount());

        if (businessProduct.getCurrency() != null)
            product.setCurrency(businessProduct.getCurrency());

        if (businessProduct.getBusinessType() != null &&
                businessProduct.getBusinessType().getId() != null &&
                (product.getBusinessType() == null ||
                        !businessProduct.getBusinessType().getId()
                                .equals(product.getBusinessType().getId()))) {

            product.setBusinessType(
                    getBusinessTypeEntity(businessProduct.getBusinessType())
            );
        }

        if (businessProduct.getBrand() != null &&
                businessProduct.getBrand().getId() != null &&
                (product.getBrand() == null ||
                        !businessProduct.getBrand().getId()
                                .equals(product.getBrand().getId()))) {

            product.setBrand(
                    getBrandEntity(businessProduct.getBrand())
            );
        }

        if (businessProduct.getCategories() != null &&
                !businessProduct.getCategories().isEmpty()) {

            Set<Long> newCategoryIds = businessProduct.getCategories().stream()
                    .map(Category::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<Long> existingCategoryIds = product.getCategories().stream()
                    .map(CategoryEntity::getId)
                    .collect(Collectors.toSet());

            if (!existingCategoryIds.equals(newCategoryIds)) {
                product.setCategories(
                        categoryJpaRepository.findAllByIdIn(newCategoryIds)
                );
            }
        } else {
            product.setCategories(new ArrayList<>());
        }

        return mapper.toDomain(repository.save(product));
    }


    private BusinessTypeEntity getBusinessTypeEntity(BusinessType businessType) {
        if (businessType != null && businessType.getId() != null) {
            return businessTypeJpaRepository
                    .findById(businessType.getId())
                    .orElseThrow(() -> new EntityNotFoundException("BusinessType not valid"));
        }
        return null;
    }

    private BrandEntity getBrandEntity(Brand brand) {
        if (brand != null && brand.getId() != null) {
            return brandJpaRepository
                    .findById(brand.getId())
                    .orElseThrow(() -> new RuntimeException("Brand not valid"));
        }
        return null;
    }

    private BusinessProductEntity getBusinessEntity(BusinessProduct product) {
        BusinessProductEntity entity = mapper.toEntity(product);
        entity.setBusinessId(product.getBusinessId());

        List<CategoryEntity> categoryList = getCategoryList(product.getCategories());
        entity.setCategories(categoryList != null ? categoryList : new ArrayList<>());

        entity.setBusinessType(getBusinessTypeEntity(product.getBusinessType()));
        entity.setBrand(getBrandEntity(product.getBrand()));
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

    @Override
    public List<BusinessProduct> save(List<BusinessProduct> products) {
        return products.stream().map(this::save).toList();
    }


    @Override
    public List<BusinessProduct> findByBusinessTypeIdAndSearch(Long businessTypeId, String keyword) {
        return repository.searchByKeyword(keyword).stream()
                .map(mapper::toDomain)
                .filter(p -> catalogOwnership.isVisibleToCaller(p.getBusinessId()))
                .filter(p -> p.getBusinessType() == null
                        || businessTypeId.equals(p.getBusinessType().getId()))
                .toList();
    }

    @Override
    public List<BusinessProduct> findBySearch(String keyword) {
        return repository.searchByKeyword(keyword).stream()
                .map(mapper::toDomain)
                .filter(p -> catalogOwnership.isVisibleToCaller(p.getBusinessId()))
                .toList();
    }

    @Override
    public List<BusinessProduct> findByBusinessTypeId(Long businessTypeId) {
        return repository.findByBusinessType_Id(businessTypeId).stream()
                .map(mapper::toDomain)
                .filter(p -> catalogOwnership.isVisibleToCaller(p.getBusinessId()))
                .toList();
    }

    @Override
    public List<BusinessProduct> findByBusinessId(Long businessId) {
        catalogOwnership.assertCanRead(businessId);
        return repository.findByBusinessId(businessId).stream().map(mapper::toDomain).toList();
    }

    private void assertUniqueProductName(Long businessId, String name, Long excludeId) {
        boolean duplicate;
        if (excludeId == null) {
            duplicate = businessId == null
                    ? repository.existsByBusinessIdIsNullAndNameIgnoreCase(name)
                    : repository.existsByBusinessIdAndNameIgnoreCase(businessId, name);
        } else {
            duplicate = businessId == null
                    ? repository.existsByBusinessIdIsNullAndNameIgnoreCaseAndIdNot(name, excludeId)
                    : repository.existsByBusinessIdAndNameIgnoreCaseAndIdNot(businessId, name, excludeId);
        }
        catalogOwnership.assertUniqueName("Product", name, duplicate);
    }
}

