package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreProductEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsOfferedByStoreServiceIdorTest {

    @Mock private StoreProductEntityMapper mapper;
    @Mock private ProductsOfferedByStoreRepository productsOfferedByStoreRepository;
    @Mock private BusinessProductJpaRepository businessProductJpaRepository;
    @Mock private StoreItemPriceService storeItemPriceService;
    @Mock private StoreService storeService;
    @Mock private UserService userService;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private ProductsOfferedByStoreServiceImpl service;

    @Test
    void findById_deniesForeignStore() {
        StoreOfferedProductEntity entity = StoreOfferedProductEntity.builder()
                .id(5L).storeId(99L).businessProductId(1L).build();
        when(productsOfferedByStoreRepository.findById(5L)).thenReturn(Optional.of(entity));
        doThrow(new AccessDeniedException("You do not have access to store: 99"))
                .when(securityContextService).validateStoreAccess(99L);

        assertThrows(AccessDeniedException.class, () -> service.findById(5L));
        verify(securityContextService).validateStoreAccess(99L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findById_allowsAccessibleStore() {
        StoreOfferedProductEntity entity = StoreOfferedProductEntity.builder()
                .id(5L).storeId(1L).businessProductId(1L).build();
        StoreOfferedProduct domain = StoreOfferedProduct.builder().id(5L).storeId(1L).build();
        when(productsOfferedByStoreRepository.findById(5L)).thenReturn(Optional.of(entity));
        doNothing().when(securityContextService).validateStoreAccess(1L);
        when(mapper.toDomain(entity)).thenReturn(domain);

        StoreOfferedProduct result = service.findById(5L);
        assertEquals(5L, result.getId());
        verify(securityContextService).validateStoreAccess(1L);
    }

    @Test
    void deleteStoreProductById_deniesForeignStore() {
        StoreOfferedProductEntity entity = StoreOfferedProductEntity.builder()
                .id(7L).storeId(50L).build();
        when(productsOfferedByStoreRepository.findById(7L)).thenReturn(Optional.of(entity));
        doThrow(new AccessDeniedException("denied"))
                .when(securityContextService).validateStoreAccess(50L);

        assertThrows(AccessDeniedException.class, () -> service.deleteStoreProductById(7L));
        verify(productsOfferedByStoreRepository, never()).deleteStoreProductById(anyLong());
    }
}
