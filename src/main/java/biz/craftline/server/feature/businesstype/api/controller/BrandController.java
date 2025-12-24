
package biz.craftline.server.feature.businesstype.api.controller;

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
    public ResponseEntity<APIResponse<List<BrandDTO>>> getAllBrands() {
        return APIResponse.ok( brandService.findAll()
                .stream()
                .map(brandDToMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<BrandDTO>> getBrand(@PathVariable Long id) {
        Brand brand = brandService.findById(id);
        if (brand == null) return null;
        return APIResponse.ok( brandDToMapper.toDTO(brand) );
    }

    @PostMapping
    public ResponseEntity<APIResponse<BrandDTO>> addBrand(@RequestBody BrandDTO brandDTO) {
        Brand brand = brandDToMapper.toDomain(brandDTO);
        Brand saved = brandService.save(brand);
        return APIResponse.ok( brandDToMapper.toDTO(saved) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteBrand(@PathVariable Long id) {
        brandService.delete(id);
        return APIResponse.success( "Success" );
    }
}


