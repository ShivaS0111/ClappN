package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedPackageDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedPackageDTOMapper;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import biz.craftline.server.feature.businessstore.domain.service.StoreOfferedPackageService;
import biz.craftline.server.util.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/store-offered-packages")
@RequiredArgsConstructor
public class StoreOfferedPackageController {

    private final StoreOfferedPackageService packageService;

    @GetMapping("/store/{storeId}")
    @RequirePermission("package.read")
    public ResponseEntity<APIResponse<List<StoreOfferedPackageDTO>>> listByStore(@PathVariable Long storeId) {
        List<StoreOfferedPackageDTO> dtos = packageService.findPackagesByStoreId(storeId)
                .orElse(List.of())
                .stream()
                .map(StoreOfferedPackageDTOMapper::toDTO)
                .collect(Collectors.toList());
        return APIResponse.success(dtos, "Packages retrieved");
    }

    @GetMapping("/{id}")
    @RequirePermission("package.read")
    public ResponseEntity<APIResponse<StoreOfferedPackageDTO>> getPackage(@PathVariable Long id) {
        return APIResponse.success(StoreOfferedPackageDTOMapper.toDTO(packageService.findById(id)), "Package retrieved");
    }

    @PostMapping
    @RequirePermission("package.create")
    public ResponseEntity<APIResponse<StoreOfferedPackageDTO>> createPackage(@RequestBody StoreOfferedPackageDTO dto) {
        StoreOfferedPackage saved = packageService.save(toPackageModel(dto));
        return APIResponse.success(StoreOfferedPackageDTOMapper.toDTO(saved), "Package created");
    }

    @PutMapping("/{id}")
    @RequirePermission("package.update")
    public ResponseEntity<APIResponse<StoreOfferedPackageDTO>> updatePackage(
            @PathVariable Long id,
            @RequestBody StoreOfferedPackageDTO dto) {
        dto.setId(id);
        StoreOfferedPackage saved = packageService.save(toPackageModel(dto));
        return APIResponse.success(StoreOfferedPackageDTOMapper.toDTO(saved), "Package updated");
    }

    @DeleteMapping("/{id}")
    @RequirePermission("package.delete")
    public ResponseEntity<APIResponse<Void>> deletePackage(@PathVariable Long id) {
        packageService.deleteStorePackageById(id);
        return APIResponse.success(null, "Package deleted");
    }

    private StoreOfferedPackage toPackageModel(StoreOfferedPackageDTO dto) {
        Set<StoreOfferedProduct> products = new HashSet<>();
        if (dto.getProductIds() != null) {
            dto.getProductIds().forEach(pid -> {
                StoreOfferedProduct p = new StoreOfferedProduct();
                p.setId(pid);
                products.add(p);
            });
        }
        Set<StoreOfferedService> services = new HashSet<>();
        if (dto.getServiceIds() != null) {
            dto.getServiceIds().forEach(sid -> {
                StoreOfferedService s = new StoreOfferedService();
                s.setId(sid);
                services.add(s);
            });
        }
        return StoreOfferedPackageDTOMapper.toModel(dto, products, services);
    }
}
