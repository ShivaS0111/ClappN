package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedPackage;
import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedPackageDTO;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import java.util.Set;
import java.util.stream.Collectors;

public class StoreOfferedPackageDTOMapper {
    public static StoreOfferedPackageDTO toDTO(StoreOfferedPackage pkg) {
        StoreOfferedPackageDTO dto = new StoreOfferedPackageDTO();
        dto.setId(pkg.getId());
        dto.setStoreId(pkg.getStoreId());
        dto.setName(pkg.getName());
        dto.setDescription(pkg.getDescription());
        dto.setPrice(pkg.getPrice());
        dto.setAvailable(pkg.getAvailable());
        if (pkg.getProducts() != null) {
            dto.setProductIds(pkg.getProducts().stream().map(StoreOfferedProduct::getId).collect(Collectors.toSet()));
        }
        if (pkg.getServices() != null) {
            dto.setServiceIds(pkg.getServices().stream().map(StoreOfferedService::getId).collect(Collectors.toSet()));
        }
        return dto;
    }

    public static StoreOfferedPackage toModel(StoreOfferedPackageDTO dto, Set<StoreOfferedProduct> products, Set<StoreOfferedService> services) {
        StoreOfferedPackage pkg = new StoreOfferedPackage();
        pkg.setId(dto.getId());
        pkg.setStoreId(dto.getStoreId());
        pkg.setName(dto.getName());
        pkg.setDescription(dto.getDescription());
        pkg.setPrice(dto.getPrice());
        pkg.setAvailable(dto.getAvailable());
        pkg.setProducts(products);
        pkg.setServices(services);
        return pkg;
    }
}
