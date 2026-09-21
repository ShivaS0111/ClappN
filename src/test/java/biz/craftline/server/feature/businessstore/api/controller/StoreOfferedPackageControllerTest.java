package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedPackageDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import biz.craftline.server.feature.businessstore.domain.service.StoreOfferedPackageService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreOfferedPackageControllerTest {

    @Mock private StoreOfferedPackageService packageService;

    @InjectMocks
    private StoreOfferedPackageController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listByStore() {
        StoreOfferedPackage pkg = new StoreOfferedPackage();
        pkg.setId(1L);
        pkg.setStoreId(1L);
        pkg.setName("Combo");
        when(packageService.findPackagesByStoreId(1L)).thenReturn(Optional.of(List.of(pkg)));

        ResponseEntity<APIResponse<List<StoreOfferedPackageDTO>>> response = controller.listByStore(1L);
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Combo", response.getBody().getData().get(0).getName());
    }

    @Test
    void getPackage() {
        StoreOfferedPackage pkg = new StoreOfferedPackage();
        pkg.setId(1L);
        pkg.setName("Combo");
        when(packageService.findById(1L)).thenReturn(pkg);

        ResponseEntity<APIResponse<StoreOfferedPackageDTO>> response = controller.getPackage(1L);
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void createAndUpdatePackage() {
        StoreOfferedPackageDTO dto = new StoreOfferedPackageDTO();
        dto.setStoreId(1L);
        dto.setName("Combo");
        dto.setProductIds(Set.of(10L));
        dto.setServiceIds(Set.of(20L));

        StoreOfferedPackage saved = new StoreOfferedPackage();
        saved.setId(5L);
        saved.setStoreId(1L);
        saved.setName("Combo");
        when(packageService.save(any(StoreOfferedPackage.class))).thenReturn(saved);

        ResponseEntity<APIResponse<StoreOfferedPackageDTO>> created = controller.createPackage(dto);
        assertEquals(5L, created.getBody().getData().getId());

        ResponseEntity<APIResponse<StoreOfferedPackageDTO>> updated = controller.updatePackage(5L, dto);
        assertEquals(5L, updated.getBody().getData().getId());
        verify(packageService, times(2)).save(any(StoreOfferedPackage.class));
    }

    @Test
    void deletePackage() {
        ResponseEntity<APIResponse<Void>> response = controller.deletePackage(1L);
        verify(packageService).deleteStorePackageById(1L);
        assertNotNull(response.getBody());
    }
}
