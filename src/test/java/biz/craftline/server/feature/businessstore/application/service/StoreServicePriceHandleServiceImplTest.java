package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreItemPriceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
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

class StoreServicePriceHandleServiceImplTest {
    @Mock
    private StoreItemPriceHandleRepository repository;
    @Mock
    private StoreItemPriceEntityMapper mapper;
    @InjectMocks
    private StoreItemPriceServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_ReturnsSavedPrice() {
        StoreItemPrice domain = StoreItemPrice.builder().id(1L).serviceId(1L).price(10.0).build();
        StoreItemPriceEntity entity = new StoreItemPriceEntity();
        StoreItemPriceEntity savedEntity = new StoreItemPriceEntity();
        StoreItemPrice savedDomain = StoreItemPrice.builder().id(1L).serviceId(1L).price(10.0).build();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);
        StoreItemPrice result = service.save(domain);
        assertEquals(savedDomain, result);
    }

    @Test
    void findAllByProductLotId_ReturnsPriceList() {
        Long productLotId = 1L;
        StoreItemPriceEntity entity = new StoreItemPriceEntity();
        StoreItemPrice domain = StoreItemPrice.builder().id(1L).serviceId(1L).price(10.0).build();
        when(repository.findAllByProductLotId(productLotId)).thenReturn(Arrays.asList(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);
        List<StoreItemPrice> result = service.findAllByProductLotId(productLotId);
        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }

    /*@Test
    void findByServiceId_ReturnsPrice() {
        Long serviceId = 1L;
        StoreItemPriceEntity entity = new StoreItemPriceEntity();
        StoreItemPrice domain = StoreItemPrice.builder().id(1L).serviceId(serviceId).price(10.0).build();
        when(repository.findByServiceId(serviceId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);
        Optional<StoreItemPrice> result = service.findByServiceId(serviceId);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }*/

    @Test
    void findByProductLotId_ReturnsPrice() {
        Long productLotId = 1L;
        StoreItemPriceEntity entity = new StoreItemPriceEntity();
        StoreItemPrice domain = StoreItemPrice.builder().id(1L).serviceId(1L).price(10.0).build();
        when(repository.findByProductLotId(productLotId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);
        Optional<StoreItemPrice> result = service.findByProductLotId(productLotId);
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }
}

