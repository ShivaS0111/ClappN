package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.enums.Status;
import biz.craftline.server.feature.businesstype.domain.model.Brand;
import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessProductServiceImplTest {

    @Mock private BusinessProductJpaRepository repository;
    @Mock private BusinessTypeJpaRepository businessTypeJpaRepository;
    @Mock private BrandJpaRepository brandJpaRepository;
    @Mock private CategoryJpaRepository categoryJpaRepository;
    @Mock private BusinessProductEntityMapper mapper;

    @InjectMocks
    private BusinessProductServiceImpl service;

    @Test
    void findAll_mapsEntities() {
        BusinessProductEntity entity = new BusinessProductEntity();
        BusinessProduct domain = BusinessProduct.builder().id(1L).name("P").build();
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertEquals(1, service.findAll().size());
    }

    @Test
    void findById_present() {
        BusinessProductEntity entity = new BusinessProductEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(BusinessProduct.builder().id(1L).build());
        assertTrue(service.findById(1L).isPresent());
    }

    @Test
    void deleteProductById_softDeletes() {
        BusinessProductEntity entity = new BusinessProductEntity();
        entity.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        service.deleteProductById(1L);
        assertEquals(Status.DELETED.getCode(), entity.getStatus());
    }

    @Test
    void deleteProductById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteProductById(1L));
    }

    @Test
    void save_single() {
        BusinessProduct product = BusinessProduct.builder().name("P").build();
        BusinessProductEntity entity = new BusinessProductEntity();
        when(mapper.toEntity(product)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(BusinessProduct.builder().id(9L).name("P").build());

        BusinessProduct saved = service.save(product);
        assertEquals(9L, saved.getId());
    }

    @Test
    void update_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.update(BusinessProduct.builder().id(1L).build()));
    }

    @Test
    void update_updatesFields() {
        BusinessProductEntity existing = new BusinessProductEntity();
        existing.setId(1L);
        existing.setName("Old");
        existing.setCategories(new ArrayList<>());
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDomain(existing)).thenReturn(BusinessProduct.builder().id(1L).name("New").build());

        BusinessProduct updated = service.update(BusinessProduct.builder()
                .id(1L).name("New").description("d").status(1).amount(10f).currency(1L).build());
        assertEquals("New", existing.getName());
        assertEquals("New", updated.getName());
    }

    @Test
    void update_replacesBusinessTypeBrandCategories() {
        BusinessProductEntity existing = new BusinessProductEntity();
        existing.setId(1L);
        existing.setCategories(new ArrayList<>());
        BusinessTypeEntity oldType = new BusinessTypeEntity();
        oldType.setId(1L);
        existing.setBusinessType(oldType);

        BusinessTypeEntity newType = new BusinessTypeEntity();
        newType.setId(2L);
        BrandEntity brand = new BrandEntity();
        brand.setId(3L);
        CategoryEntity cat = new CategoryEntity();
        cat.setId(4L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(businessTypeJpaRepository.findById(2L)).thenReturn(Optional.of(newType));
        when(brandJpaRepository.findById(3L)).thenReturn(Optional.of(brand));
        when(categoryJpaRepository.findAllByIdIn(Set.of(4L))).thenReturn(List.of(cat));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDomain(existing)).thenReturn(BusinessProduct.builder().id(1L).build());

        service.update(BusinessProduct.builder()
                .id(1L)
                .businessType(BusinessType.builder().id(2L).build())
                .brand(Brand.builder().id(3L).build())
                .categories(List.of(Category.builder().id(4L).build()))
                .build());

        assertEquals(2L, existing.getBusinessType().getId());
        assertEquals(3L, existing.getBrand().getId());
        assertEquals(1, existing.getCategories().size());
    }

    @Test
    void findByBusinessTypeId() {
        when(repository.findByBusinessType_Id(5L)).thenReturn(List.of(new BusinessProductEntity()));
        when(mapper.toDomain(any())).thenReturn(BusinessProduct.builder().id(1L).build());
        assertEquals(1, service.findByBusinessTypeId(5L).size());
    }

    @Test
    void findBySearch_andByKeyword() {
        when(repository.findAll()).thenReturn(List.of(new BusinessProductEntity()));
        when(repository.searchByKeyword("x")).thenReturn(List.of(new BusinessProductEntity()));
        when(mapper.toDomain(any())).thenReturn(BusinessProduct.builder().id(1L).build());
        assertEquals(1, service.findBySearch("x").size());
        assertEquals(1, service.findByBusinessTypeIdAndSearch(1L, "x").size());
    }

    @Test
    void save_list_skipsFailures() {
        BusinessProduct p = BusinessProduct.builder().name("P").build();
        BusinessProductEntity entity = new BusinessProductEntity();
        when(mapper.toEntity(p)).thenReturn(entity);
        when(repository.save(entity)).thenThrow(new RuntimeException("dup"));
        when(mapper.toDomain(entity)).thenReturn(p);

        assertTrue(service.save(List.of(p)).isEmpty());
    }
}
