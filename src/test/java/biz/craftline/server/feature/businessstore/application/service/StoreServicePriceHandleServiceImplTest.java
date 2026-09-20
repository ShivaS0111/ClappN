package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.enums.Item;
import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreItemPriceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
import biz.craftline.server.feature.inventorymanagement.infra.repository.ProductLotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class StoreServicePriceHandleServiceImplTest {
    @Mock private StoreItemPriceHandleRepository repository;
    @Mock private StoreItemPriceEntityMapper mapper;
    @Mock private ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;
    @Mock private ProductsOfferedByStoreRepository productsOfferedByStoreRepository;
    @Mock private ProductLotRepository productLotRepository;
    @Mock private SecurityContextService securityContextService;
    @InjectMocks private StoreItemPriceServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(securityContextService).validateStoreAccess(any());
    }

    @Test
    void save_ReturnsSavedPrice() {
        StoreItemPrice domain = StoreItemPrice.builder()
                .id(1L)
                .itemId(1L)
                .itemType(Item.SERVICE.getType())
                .price(10.0)
                .build();
        StoreItemPriceEntity entity = new StoreItemPriceEntity();
        StoreItemPriceEntity savedEntity = new StoreItemPriceEntity();
        StoreItemPrice savedDomain = StoreItemPrice.builder()
                .id(1L)
                .itemId(1L)
                .itemType(Item.SERVICE.getType())
                .price(10.0)
                .build();

        StoreOfferedServiceEntity svc = new StoreOfferedServiceEntity();
        svc.setId(1L);
        svc.setStoreId(5L);
        when(servicesOfferedByStoreRepository.findById(1L)).thenReturn(Optional.of(svc));
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        assertEquals(savedDomain, service.save(domain));
    }
}
