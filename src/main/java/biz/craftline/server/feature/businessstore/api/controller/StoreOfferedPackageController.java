package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedPackageDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreOfferedPackageDTOMapper;
import biz.craftline.server.feature.businessstore.domain.service.StoreOfferedPackageService;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/store-offered-packages")
public class StoreOfferedPackageController {
    private final StoreOfferedPackageService packageService;
    // You would inject services for StoreOfferedProduct/Service lookup here in a real app

    public StoreOfferedPackageController(StoreOfferedPackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public List<StoreOfferedPackageDTO> getAllPackages() {
        List<StoreOfferedPackage> packages = packageService.getAllPackages();
        List<StoreOfferedPackageDTO> dtos = new ArrayList<>();
        for (StoreOfferedPackage pkg : packages) {
            dtos.add(StoreOfferedPackageDTOMapper.toDTO(pkg));
        }
        return dtos;
    }

    @GetMapping("/{id}")
    public StoreOfferedPackageDTO getPackage(@PathVariable Long id) {
        StoreOfferedPackage pkg = packageService.getPackage(id);
        return pkg != null ? StoreOfferedPackageDTOMapper.toDTO(pkg) : null;
    }

    @PostMapping
    public StoreOfferedPackageDTO createPackage(@RequestBody StoreOfferedPackageDTO dto) {
        // In a real app, fetch products/services by IDs
        Set<StoreOfferedProduct> products = new HashSet<>();
        Set<StoreOfferedService> services = new HashSet<>();
        StoreOfferedPackage pkg = StoreOfferedPackageDTOMapper.toModel(dto, products, services);
        StoreOfferedPackage saved = packageService.savePackage(pkg);
        return StoreOfferedPackageDTOMapper.toDTO(saved);
    }

    @DeleteMapping("/{id}")
    public void deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
    }
}
