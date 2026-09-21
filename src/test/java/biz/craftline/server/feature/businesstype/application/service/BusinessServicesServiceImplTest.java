package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.enums.Status;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.entity.CategoryEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessServiceEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
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
class BusinessServicesServiceImplTest {

    @Mock private BusinessServicesJpaRepository repository;
    @Mock private BusinessTypeJpaRepository businessTypeJpaRepository;
    @Mock private BusinessServiceEntityMapper mapper;
    @Mock private CategoryJpaRepository categoryJpaRepository;

    @InjectMocks
    private BusinessServicesServiceImpl service;

    @Test
    void findAll() {
        when(repository.findAll()).thenReturn(List.of(new BusinessServiceEntity()));
        when(mapper.toDomain(any())).thenReturn(BusinessService.builder().id(1L).build());
        assertEquals(1, service.findAll().size());
    }

    @Test
    void findById() {
        BusinessServiceEntity entity = new BusinessServiceEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(BusinessService.builder().id(1L).build());
        assertTrue(service.findById(1L).isPresent());
    }

    @Test
    void deleteServiceById_softDeletes() {
        BusinessServiceEntity entity = new BusinessServiceEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        service.deleteServiceById(1L);
        assertEquals(Status.DELETED.getCode(), entity.getStatus());
    }

    @Test
    void deleteServiceById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.deleteServiceById(1L));
    }

    @Test
    void save_requiresBusinessType() {
        BusinessService bs = BusinessService.builder().serviceName("Cut").build();
        when(mapper.toEntity(bs)).thenReturn(new BusinessServiceEntity());
        assertThrows(IllegalArgumentException.class, () -> service.save(bs));
    }

    @Test
    void save_success() {
        BusinessService bs = BusinessService.builder()
                .serviceName("Cut")
                .businessType(BusinessType.builder().id(2L).build())
                .build();
        BusinessServiceEntity entity = new BusinessServiceEntity();
        BusinessTypeEntity type = new BusinessTypeEntity();
        type.setId(2L);
        when(mapper.toEntity(bs)).thenReturn(entity);
        when(businessTypeJpaRepository.findById(2L)).thenReturn(Optional.of(type));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(BusinessService.builder().id(9L).build());

        assertEquals(9L, service.save(bs).getId());
    }

    @Test
    void save_businessTypeMissing_throws() {
        BusinessService bs = BusinessService.builder()
                .serviceName("Cut")
                .businessType(BusinessType.builder().id(2L).build())
                .build();
        when(mapper.toEntity(bs)).thenReturn(new BusinessServiceEntity());
        when(businessTypeJpaRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.save(bs));
    }

    @Test
    void update_success() {
        BusinessServiceEntity existing = new BusinessServiceEntity();
        existing.setId(1L);
        existing.setCategories(new ArrayList<>());
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDomain(existing)).thenReturn(BusinessService.builder().id(1L).serviceName("New").build());

        BusinessService result = service.update(BusinessService.builder()
                .id(1L).serviceName("New").description("d").status(1).amount(5f).currency(1L).build());
        assertEquals("New", result.getServiceName());
    }

    @Test
    void update_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.update(BusinessService.builder().id(1L).build()));
    }

    @Test
    void update_categoriesAndType() {
        BusinessServiceEntity existing = new BusinessServiceEntity();
        existing.setId(1L);
        existing.setCategories(new ArrayList<>());
        BusinessTypeEntity old = new BusinessTypeEntity();
        old.setId(1L);
        existing.setBusinessType(old);

        BusinessTypeEntity neu = new BusinessTypeEntity();
        neu.setId(2L);
        CategoryEntity cat = new CategoryEntity();
        cat.setId(4L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(businessTypeJpaRepository.findById(2L)).thenReturn(Optional.of(neu));
        when(categoryJpaRepository.findAllByIdIn(Set.of(4L))).thenReturn(List.of(cat));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDomain(existing)).thenReturn(BusinessService.builder().id(1L).build());

        service.update(BusinessService.builder()
                .id(1L)
                .status(1)
                .businessType(BusinessType.builder().id(2L).build())
                .categories(List.of(Category.builder().id(4L).build()))
                .build());
        assertEquals(2L, existing.getBusinessType().getId());
    }

    @Test
    void searchAndFindHelpers() {
        when(repository.searchByKeywordAndBusinessType("k", 1L)).thenReturn(List.of(new BusinessServiceEntity()));
        when(repository.findAll()).thenReturn(List.of(new BusinessServiceEntity()));
        when(repository.findByBusinessType_Id(1L)).thenReturn(List.of(new BusinessServiceEntity()));
        when(mapper.toDomain(any())).thenReturn(BusinessService.builder().id(1L).build());

        assertEquals(1, service.searchByKeywordAndBusinessType(1L, "k").size());
        assertEquals(1, service.findBySearch("k").size());
        assertEquals(1, service.findByBusinessTypeId(1L).size());
    }
}
