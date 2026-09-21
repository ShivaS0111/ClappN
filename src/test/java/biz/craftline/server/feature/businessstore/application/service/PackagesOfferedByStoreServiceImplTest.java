package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedPackageEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreOfferedPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class PackagesOfferedByStoreServiceImplTest {

    @Mock private StoreOfferedPackageRepository repository;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private PackagesOfferedByStoreServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(securityContextService).validateStoreAccess(anyLong());
    }

    @Test
    void findPackagesByStoreId() {
        StoreOfferedPackageEntity entity = StoreOfferedPackageEntity.builder()
                .id(1L).storeId(1L).name("Combo").productIds(Set.of(10L)).serviceIds(Set.of(20L)).build();
        when(repository.findByStoreId(1L)).thenReturn(List.of(entity));

        Optional<List<StoreOfferedPackage>> result = service.findPackagesByStoreId(1L);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("Combo", result.get().get(0).getName());
        assertEquals(1, result.get().get(0).getProducts().size());
        assertEquals(1, result.get().get(0).getServices().size());
        verify(securityContextService).validateStoreAccess(1L);
    }

    @Test
    void findById_returnsPackage() {
        StoreOfferedPackageEntity entity = StoreOfferedPackageEntity.builder()
                .id(1L).storeId(1L).name("Combo").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        StoreOfferedPackage pkg = service.findById(1L);
        assertEquals("Combo", pkg.getName());
    }

    @Test
    void findById_throwsWhenMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.findById(9L));
    }

    @Test
    void save_mapsProductsAndServices() {
        StoreOfferedProduct product = new StoreOfferedProduct();
        product.setId(10L);
        StoreOfferedService svc = new StoreOfferedService();
        svc.setId(20L);
        StoreOfferedPackage pkg = new StoreOfferedPackage();
        pkg.setStoreId(1L);
        pkg.setName("Pkg");
        pkg.setProducts(Set.of(product));
        pkg.setServices(Set.of(svc));

        when(repository.save(any(StoreOfferedPackageEntity.class))).thenAnswer(inv -> {
            StoreOfferedPackageEntity e = inv.getArgument(0);
            e.setId(5L);
            return e;
        });

        StoreOfferedPackage saved = service.save(pkg);
        assertEquals(5L, saved.getId());
        assertEquals(Set.of(10L), saved.getProducts().stream().map(StoreOfferedProduct::getId).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void save_list() {
        StoreOfferedPackage pkg = new StoreOfferedPackage();
        pkg.setStoreId(1L);
        pkg.setName("Pkg");
        when(repository.save(any())).thenAnswer(inv -> {
            StoreOfferedPackageEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        assertEquals(1, service.save(List.of(pkg)).size());
    }

    @Test
    void deleteStorePackageById() {
        StoreOfferedPackageEntity entity = StoreOfferedPackageEntity.builder().id(1L).storeId(1L).name("X").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteStorePackageById(1L);
        verify(repository).delete(entity);
        verify(securityContextService).validateStoreAccess(1L);
    }

    @Test
    void deleteStorePackageById_throwsWhenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteStorePackageById(1L));
    }
}
