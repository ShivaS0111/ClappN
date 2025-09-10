package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.BusinessEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.BusinessEntityJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessEntityServiceImplTest {
    @Mock
    private BusinessEntityJpaRepository businessEntityRepository;
    @Mock
    private BusinessEntityMapper mapper;
    @InjectMocks
    private BusinessEntityServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_ReturnsBusinessList() {
        BusinessEntity entity1 = new BusinessEntity();
        BusinessEntity entity2 = new BusinessEntity();
        Business business1 = Business.builder().id(1L).businessName("A").build();
        Business business2 = Business.builder().id(2L).businessName("B").build();
        when(businessEntityRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(business1);
        when(mapper.toDomain(entity2)).thenReturn(business2);
        List<Business> result = service.findAll();
        assertEquals(2, result.size());
        assertEquals(business1, result.get(0));
        assertEquals(business2, result.get(1));
    }

    @Test
    void deleteBusinessTypeById_CallsRepository() {
        Long id = 1L;
        service.deleteBusinessTypeById(id);
        verify(businessEntityRepository).deleteBusinessTypeById(id);
    }

    @Test
    void findById_ReturnsBusiness() {
        Long id = 1L;
        BusinessEntity entity = new BusinessEntity();
        Business business = Business.builder().id(id).businessName("A").build();
        when(businessEntityRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(business);
        Optional<Business> result = service.findById(id);
        assertTrue(result.isPresent());
        assertEquals(business, result.get());
    }

    @Test
    void save_ReturnsSavedBusiness() {
        Business business = Business.builder().id(1L).businessName("A").build();
        BusinessEntity entity = new BusinessEntity();
        BusinessEntity savedEntity = new BusinessEntity();
        Business savedBusiness = Business.builder().id(1L).businessName("A").build();
        when(mapper.toEntity(business)).thenReturn(entity);
        when(businessEntityRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedBusiness);
        Business result = service.save(business);
        assertEquals(savedBusiness, result);
    }

    @Test
    void search_ReturnsBusinessList() {
        String keyword = "test";
        BusinessEntity entity = new BusinessEntity();
        Business business = Business.builder().id(1L).businessName("Test Business").build();
        when(businessEntityRepository.findByNameContaining(keyword)).thenReturn(Arrays.asList(entity));
        when(mapper.toDomain(entity)).thenReturn(business);
        List<Business> result = service.search(keyword);
        assertEquals(1, result.size());
        assertEquals(business, result.get(0));
    }
}

