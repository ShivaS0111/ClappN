package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreOfferedServiceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessServicesJpaRepository;
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

class ServicesOfferedByStoreServiceImplTest {
    @Mock
    private ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;
    @Mock
    private StoreOfferedServiceEntityMapper mapper;
    @Mock
    private BusinessServicesJpaRepository businessServicesJpaRepository;
    @InjectMocks
    private ServicesOfferedByStoreServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deleteStoreServiceById_CallsRepository() {
        Long id = 1L;
        service.deleteStoreServiceById(id);
        verify(servicesOfferedByStoreRepository).deleteStoreServiceById(id);
    }

    @Test
    void findServicesByStoreId_ReturnsServiceList() {
        Long storeId = 1L;
        StoreOfferedServiceEntity entity1 = new StoreOfferedServiceEntity();
        StoreOfferedServiceEntity entity2 = new StoreOfferedServiceEntity();
        StoreOfferedService service1 = StoreOfferedService.builder().id(1L).storeId(storeId).aliasName("A").build();
        StoreOfferedService service2 = StoreOfferedService.builder().id(2L).storeId(storeId).aliasName("B").build();
        when(servicesOfferedByStoreRepository.findServicesByStoreId(storeId)).thenReturn(Optional.of(Arrays.asList(entity1, entity2)));
        when(mapper.toDomain(entity1)).thenReturn(service1);
        when(mapper.toDomain(entity2)).thenReturn(service2);
        Optional<List<StoreOfferedService>> result = service.findServicesByStoreId(storeId);
        assertTrue(result.isPresent());
        List<StoreOfferedService> sorted = result.get().stream().sorted((a, b) -> Long.compare(a.getId(), b.getId())).toList();
        assertEquals(2, sorted.size());
        assertEquals(service1, sorted.get(0));
        assertEquals(service2, sorted.get(1));
    }

    @Test
    void save_ReturnsSavedService() {
        StoreOfferedService domain = StoreOfferedService.builder().id(1L).storeId(1L).aliasName("A").build();
        StoreOfferedServiceEntity entity = new StoreOfferedServiceEntity();
        StoreOfferedServiceEntity savedEntity = new StoreOfferedServiceEntity();
        StoreOfferedService savedDomain = StoreOfferedService.builder().id(1L).storeId(1L).aliasName("A").build();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(servicesOfferedByStoreRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);
        StoreOfferedService result = service.save(domain);
        assertEquals(savedDomain, result);
    }
}
