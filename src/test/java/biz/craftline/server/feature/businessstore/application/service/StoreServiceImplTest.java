package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.BusinessEntityJpaRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
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

class StoreServiceImplTest {
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreEntityMapper storeEntityMapper;
    @Mock
    private BusinessEntityJpaRepository businessRepository;
    @InjectMocks
    private StoreServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_ReturnsStoreList() {
        StoreEntity entity1 = new StoreEntity();
        StoreEntity entity2 = new StoreEntity();
        Store store1 = Store.builder().id(1L).storeName("A").build();
        Store store2 = Store.builder().id(2L).storeName("B").build();
        when(storeRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));
        when(storeEntityMapper.toDomain(entity1)).thenReturn(store1);
        when(storeEntityMapper.toDomain(entity2)).thenReturn(store2);
        List<Store> result = service.findAll();
        result.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        assertEquals(2, result.size());
        //assertEquals(store1, result.get(0));
        //assertEquals(store2, result.get(1));
    }

    @Test
    void searchStores_ReturnsStoreList() {
        String keyword = "test";
        StoreEntity entity = new StoreEntity();
        Store store = Store.builder().id(1L).storeName("Test Store").build();
        when(storeRepository.searchStoreByStoreName(keyword)).thenReturn(Arrays.asList(entity));
        when(storeEntityMapper.toDomain(entity)).thenReturn(store);
        List<Store> result = service.searchStores(keyword);
        assertEquals(1, result.size());
        assertEquals(store, result.get(0));
    }

    @Test
    void findStoresByBusiness_ReturnsStoreList() {
        long businessId = 1L;
        StoreEntity entity = new StoreEntity();
        Store store = Store.builder().id(1L).storeName("Store").build();
        when(storeRepository.findByBusinessId(businessId)).thenReturn(Arrays.asList(entity));
        when(storeEntityMapper.toDomain(entity)).thenReturn(store);
        List<Store> result = service.findStoresByBusiness(businessId);
        assertEquals(1, result.size());
        assertEquals(store, result.get(0));
    }

    @Test
    void deleteBusinessTypeById_CallsRepository() {
        Long id = 1L;
        service.deleteBusinessTypeById(id);
        verify(storeRepository).deleteBusinessTypeById(id);
    }

    @Test
    void findById_ReturnsStore() {
        Long id = 1L;
        StoreEntity entity = new StoreEntity();
        Store store = Store.builder().id(id).storeName("A").build();
        when(storeRepository.findById(id)).thenReturn(Optional.of(entity));
        when(storeEntityMapper.toDomain(entity)).thenReturn(store);
        Optional<Store> result = service.findById(id);
        assertTrue(result.isPresent());
        assertEquals(store, result.get());
    }
}
