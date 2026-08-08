package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreItemPriceEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.ServicesOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
import biz.craftline.server.feature.inventorymanagement.infra.repository.ProductLotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreItemPriceServiceIdorTest {

    @Mock private ServicesOfferedByStoreRepository servicesOfferedByStoreRepository;
    @Mock private ProductsOfferedByStoreRepository productsOfferedByStoreRepository;
    @Mock private ProductLotRepository productLotRepository;
    @Mock private StoreItemPriceEntityMapper mapper;
    @Mock private StoreItemPriceHandleRepository repository;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private StoreItemPriceServiceImpl service;

    @Test
    void findByLotId_deniesForeignStoreProduct() {
        when(productLotRepository.findById(5L)).thenReturn(Optional.empty());
        StoreOfferedProductEntity product = StoreOfferedProductEntity.builder()
                .id(5L).storeId(77L).build();
        when(productsOfferedByStoreRepository.findById(5L)).thenReturn(Optional.of(product));
        doThrow(new AccessDeniedException("denied"))
                .when(securityContextService).validateStoreAccess(77L);

        assertThrows(AccessDeniedException.class, () -> service.findByLotId(5L));
        verify(repository, never()).findByItemIdAndItemType(anyLong(), anyLong());
    }
}
