package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreProductEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductsOfferedByStoreAssignValidationTest {

    @Mock private StoreProductEntityMapper mapper;
    @Mock private ProductsOfferedByStoreRepository productsOfferedByStoreRepository;
    @Mock private BusinessProductJpaRepository businessProductJpaRepository;
    @Mock private StoreItemPriceService storeItemPriceService;
    @Mock private StoreService storeService;
    @Mock private UserService userService;
    @Mock private SecurityContextService securityContextService;

    private ProductsOfferedByStoreServiceImpl service;

    @BeforeEach
    void setUp() {
        StoreOfferingAssignSupport assignSupport =
                new StoreOfferingAssignSupport(storeService, securityContextService);
        service = new ProductsOfferedByStoreServiceImpl(
                mapper,
                productsOfferedByStoreRepository,
                businessProductJpaRepository,
                storeItemPriceService,
                storeService,
                userService,
                securityContextService,
                assignSupport);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("owner@test.com", null, List.of()));
        User user = new User();
        user.setId(9L);
        user.setEmail("owner@test.com");
        when(userService.getUserByEmail("owner@test.com")).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void save_rejectsMissingMasterProduct() {
        StoreOfferedProduct domain = StoreOfferedProduct.builder()
                .storeId(1L).businessProductId(99L).build();
        when(storeService.findById(1L)).thenReturn(Optional.of(store(1L, 10L, 2L)));
        when(businessProductJpaRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(domain));
        assertTrue(ex.getMessage().contains("Business product not found"));
        verify(productsOfferedByStoreRepository, never()).save(any());
    }

    @Test
    void save_rejectsBusinessMismatch() {
        StoreOfferedProduct domain = StoreOfferedProduct.builder()
                .storeId(1L).businessId(999L).businessProductId(5L).build();
        when(storeService.findById(1L)).thenReturn(Optional.of(store(1L, 10L, 2L)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(domain));
        assertTrue(ex.getMessage().contains("does not match"));
        verify(productsOfferedByStoreRepository, never()).save(any());
    }

    @Test
    void save_rejectsDuplicateAssign() {
        StoreOfferedProduct domain = StoreOfferedProduct.builder()
                .storeId(1L).businessProductId(5L).build();
        when(storeService.findById(1L)).thenReturn(Optional.of(store(1L, 10L, 2L)));
        when(businessProductJpaRepository.findById(5L)).thenReturn(Optional.of(master(5L, 2L)));
        when(productsOfferedByStoreRepository.findByStoreIdAndBusinessProductId(1L, 5L))
                .thenReturn(Optional.of(StoreOfferedProductEntity.builder().id(77L).build()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(domain));
        assertTrue(ex.getMessage().contains("already offered"));
        verify(productsOfferedByStoreRepository, never()).save(any());
    }

    @Test
    void save_succeedsAndFillsBusinessId() {
        StoreOfferedProduct domain = StoreOfferedProduct.builder()
                .storeId(1L).businessProductId(5L).aliasName("Soap").build();
        StoreOfferedProductEntity entity = StoreOfferedProductEntity.builder().id(1L).storeId(1L).build();
        StoreOfferedProduct saved = StoreOfferedProduct.builder().id(1L).storeId(1L).businessId(10L).build();

        when(storeService.findById(1L)).thenReturn(Optional.of(store(1L, 10L, 2L)));
        when(businessProductJpaRepository.findById(5L)).thenReturn(Optional.of(master(5L, 2L)));
        when(productsOfferedByStoreRepository.findByStoreIdAndBusinessProductId(1L, 5L))
                .thenReturn(Optional.empty());
        when(mapper.toEntity(any())).thenReturn(entity);
        when(productsOfferedByStoreRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(saved);

        StoreOfferedProduct result = service.save(domain);
        assertEquals(10L, domain.getBusinessId());
        assertEquals(1L, result.getId());
        verify(securityContextService).validateStoreAccess(1L);
        verify(securityContextService).validateBusinessAccess(10L);
    }

    private static Store store(Long id, Long businessId, Long businessType) {
        return Store.builder()
                .id(id)
                .businessType(businessType)
                .business(Business.builder().id(businessId).build())
                .build();
    }

    private static BusinessProductEntity master(Long id, Long typeId) {
        BusinessProductEntity e = new BusinessProductEntity();
        e.setId(id);
        BusinessTypeEntity type = new BusinessTypeEntity();
        type.setId(typeId);
        e.setBusinessType(type);
        return e;
    }
}
