package biz.craftline.server.feature.businesstype.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.businesstype.api.dto.BrandDTO;
import biz.craftline.server.feature.businesstype.api.mapper.BrandDToMapper;
import biz.craftline.server.feature.businesstype.domain.model.Brand;
import biz.craftline.server.feature.businesstype.domain.service.BrandService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/api/brands")
public class BrandController {

    final BrandService brandService;

    final BrandDToMapper brandDToMapper;

    @GetMapping
    @RequirePermission("brand.read")
    public ResponseEntity<APIResponse<List<BrandDTO>>> getAllBrands() {
        return APIResponse.ok( brandService.findAll()
                .stream()
                .map(brandDToMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @RequirePermission("brand.read")
    public ResponseEntity<APIResponse<BrandDTO>> getBrand(@PathVariable Long id) {
        Brand brand = brandService.findById(id);
        if (brand == null) {
            return APIResponse.error("Brand not found", org.springframework.http.HttpStatus.NOT_FOUND);
        }
        return APIResponse.ok(brandDToMapper.toDTO(brand));
    }

    @PostMapping
    @RequirePermission("brand.create")
    public ResponseEntity<APIResponse<BrandDTO>> addBrand(@RequestBody BrandDTO brandDTO) {
        Brand brand = brandDToMapper.toDomain(brandDTO);
        Brand saved = brandService.save(brand);
        return APIResponse.ok(brandDToMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @RequirePermission("brand.update")
    public ResponseEntity<APIResponse<BrandDTO>> updateBrand(@PathVariable Long id, @RequestBody BrandDTO brandDTO) {
        Brand brand = brandDToMapper.toDomain(brandDTO);
        Brand updated = brandService.update(id, brand);
        return APIResponse.ok(brandDToMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("brand.delete")
    public ResponseEntity<APIResponse<String>> deleteBrand(@PathVariable Long id) {
        brandService.delete(id);
        return APIResponse.success("Success");
    }
}
