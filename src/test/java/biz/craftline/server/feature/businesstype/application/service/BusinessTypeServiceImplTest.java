package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.mapper.BusinessTypeEntityMapper;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessTypeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusinessTypeServiceImplTest {

    @Mock private BusinessTypeJpaRepository repository;
    @Mock private BusinessTypeEntityMapper mapper;

    @InjectMocks
    private BusinessTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll() {
        BusinessTypeEntity entity = new BusinessTypeEntity();
        entity.setId(1L);
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(BusinessType.builder().id(1L).businessName("Salon").build());

        assertEquals(1, service.findAll().size());
    }

    @Test
    void findByNameContaining() {
        BusinessTypeEntity entity = new BusinessTypeEntity();
        when(repository.findByNameContaining("Sal")).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(BusinessType.builder().id(1L).businessName("Salon").build());
        assertEquals(1, service.findByNameContaining("Sal").size());
    }

    @Test
    void deleteBusinessTypeById() {
        BusinessTypeEntity entity = new BusinessTypeEntity();
        entity.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        service.deleteBusinessTypeById(1L);
        verify(repository).delete(entity);
    }

    @Test
    void deleteBusinessTypeById_throwsWhenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.deleteBusinessTypeById(1L));
    }

    @Test
    void findById() {
        BusinessTypeEntity entity = new BusinessTypeEntity();
        entity.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(BusinessType.builder().id(1L).build());
        assertTrue(service.findById(1L).isPresent());
    }

    @Test
    void save_andUpdate() {
        BusinessType domain = BusinessType.builder().businessName("Salon").build();
        BusinessTypeEntity entity = new BusinessTypeEntity();
        entity.setId(1L);
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(BusinessType.builder().id(1L).businessName("Salon").build());

        assertEquals(1L, service.save(domain).getId());
        assertEquals(1L, service.update(domain).getId());
    }

    @Test
    void findAllByIds() {
        BusinessTypeEntity entity = new BusinessTypeEntity();
        entity.setId(1L);
        when(repository.findAllById(List.of(1L))).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(BusinessType.builder().id(1L).build());
        assertEquals(1, service.findAllByIds(List.of(1L)).size());
    }
}
