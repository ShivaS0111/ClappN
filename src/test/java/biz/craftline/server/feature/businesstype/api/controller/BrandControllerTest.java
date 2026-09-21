package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.feature.businesstype.api.dto.BrandDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BrandDToMapper;
import biz.craftline.server.feature.businesstype.domain.model.Brand;
import biz.craftline.server.feature.businesstype.domain.service.BrandService;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BrandControllerTest {

    @Mock private BrandService brandService;
    @Mock private BrandDToMapper brandDToMapper;

    @InjectMocks
    private BrandController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllBrands() {
        Brand brand = Brand.builder().id(1L).name("Nike").build();
        BrandDTO dto = new BrandDTO();
        dto.setId(1L);
        dto.setName("Nike");
        when(brandService.findAll()).thenReturn(List.of(brand));
        when(brandDToMapper.toDTO(brand)).thenReturn(dto);

        ResponseEntity<APIResponse<List<BrandDTO>>> response = controller.getAllBrands();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getBrand_found() {
        Brand brand = Brand.builder().id(1L).name("Nike").build();
        BrandDTO dto = new BrandDTO();
        dto.setId(1L);
        when(brandService.findById(1L)).thenReturn(brand);
        when(brandDToMapper.toDTO(brand)).thenReturn(dto);

        ResponseEntity<APIResponse<BrandDTO>> response = controller.getBrand(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void getBrand_notFound() {
        when(brandService.findById(9L)).thenReturn(null);
        ResponseEntity<APIResponse<BrandDTO>> response = controller.getBrand(9L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void addBrand() {
        BrandDTO dto = new BrandDTO();
        dto.setName("Nike");
        Brand domain = Brand.builder().name("Nike").build();
        Brand saved = Brand.builder().id(1L).name("Nike").build();
        BrandDTO savedDto = new BrandDTO();
        savedDto.setId(1L);
        when(brandDToMapper.toDomain(dto)).thenReturn(domain);
        when(brandService.save(domain)).thenReturn(saved);
        when(brandDToMapper.toDTO(saved)).thenReturn(savedDto);

        ResponseEntity<APIResponse<BrandDTO>> response = controller.addBrand(dto);
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void updateBrand() {
        BrandDTO dto = new BrandDTO();
        dto.setName("Updated");
        Brand domain = Brand.builder().name("Updated").build();
        Brand updated = Brand.builder().id(1L).name("Updated").build();
        BrandDTO updatedDto = new BrandDTO();
        updatedDto.setId(1L);
        updatedDto.setName("Updated");
        when(brandDToMapper.toDomain(dto)).thenReturn(domain);
        when(brandService.update(eq(1L), any())).thenReturn(updated);
        when(brandDToMapper.toDTO(updated)).thenReturn(updatedDto);

        ResponseEntity<APIResponse<BrandDTO>> response = controller.updateBrand(1L, dto);
        assertEquals("Updated", response.getBody().getData().getName());
    }

    @Test
    void deleteBrand() {
        ResponseEntity<APIResponse<String>> response = controller.deleteBrand(1L);
        verify(brandService).delete(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
