package biz.craftline.server.feature.inventorymanagement.api.mapper;

import biz.craftline.server.feature.inventorymanagement.api.dto.ProductLotDTO;
import biz.craftline.server.feature.inventorymanagement.api.request.AddProductLotRequest;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLot;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductLotDTOMapper {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public ProductLotDTO toDTO(ProductLot source) {
        if (source == null) return null;

        ProductLotDTO target = new ProductLotDTO();
        target.setId(source.getId());  // Added id mapping
        target.setProductId(source.getProductId());
        target.setStoreId(source.getStoreId());
        target.setLotCode(source.getLotCode());
        target.setQuantity(source.getQuantity());
        target.setBlocked(source.getBlocked());
        target.setSold(source.getSold());
        target.setUnitPrice(source.getUnitPrice());
        target.setCurrency(source.getCurrency());
        target.setCountry(source.getCountry());
        target.setActive(source.isActive());
        target.setPurchasedAt(source.getPurchasedAt());
        target.setMfgDate(source.getMfgDate());
        target.setExpiryAt(source.getExpiryAt());

        return target;
    }

    public List<ProductLotDTO> toDTOList(List<ProductLot> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductLot toDomain(ProductLotDTO source) {
        if (source == null) return null;

        ProductLot target = new ProductLot();
        target.setId(source.getId());  // Added id mapping
        target.setProductId(source.getProductId());
        target.setStoreId(source.getStoreId());
        target.setLotCode(source.getLotCode());
        target.setQuantity(source.getQuantity());
        target.setBlocked(source.getBlocked());
        target.setSold(source.getSold());
        target.setUnitPrice(source.getUnitPrice());
        target.setCurrency(source.getCurrency());
        target.setCountry(source.getCountry());
        target.setActive(source.isActive());
        target.setPurchasedAt(source.getPurchasedAt());
        target.setMfgDate(source.getMfgDate());
        target.setExpiryAt(source.getExpiryAt());

        return target;
    }

    public List<ProductLot> toDomainList(List<ProductLotDTO> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public ProductLot toDomain(AddProductLotRequest source) {
        if (source == null) return null;

        ProductLot target = new ProductLot();
        target.setProductId(source.getProductId());
        target.setStoreId(source.getStoreId());
        target.setLotCode(source.getLotCode());
        target.setQuantity(source.getQuantity());
        target.setBlocked(source.getBlocked());
        target.setSold(source.getSold());
        target.setUnitPrice(source.getUnitPrice());
        target.setCurrency(source.getCurrency());
        target.setCountry(source.getCountry());
        target.setActive(source.isActive());
        target.setPurchasedAt(source.getPurchasedAt());
        target.setMfgDate(source.getMfgDate());
        target.setExpiryAt(source.getExpiryAt());

        validateProductLot(target);

        return target;
    }

    private void validateProductLot(ProductLot lot) {
        if (lot.getProductId() == null) throw new IllegalArgumentException("Product ID cannot be null");

        if (lot.getQuantity() < 0) throw new IllegalArgumentException("Quantity cannot be negative");

        if (lot.getBlocked() < 0) throw new IllegalArgumentException("Blocked quantity cannot be negative");

        if (lot.getSold() < 0) throw new IllegalArgumentException("Sold quantity cannot be negative");

        if (lot.getUnitPrice() < 0) throw new IllegalArgumentException("Unit price cannot be negative");
    }
}
