package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.ProductLot;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ProductLotEntityMapper {

    private final ProductsOfferedByStoreRepository repository;

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private String formatDateTime(Date dateTime) {
        return dateTime != null ? DATE_FORMATTER.format(dateTime) : null;
    }

    private Date parseDateTime(String dateTimeStr) {
        try {
            return dateTimeStr != null ? DATE_FORMATTER.parse(dateTimeStr) : null;
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + dateTimeStr, e);
        }
    }

    public ProductLotEntity toEntity(ProductLot source) {
        if (source == null) return null;

        if(source.getProductId()==null) throw  new RuntimeException("Product ID is required");

        StoreOfferedProductEntity productEntity = repository.findById(source.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product with ID " + source.getProductId() + " not found"
                ));
        ProductLotEntity target = new ProductLotEntity();
        target.setId(source.getId());  // Added id mapping
        target.setProduct(productEntity);
        target.setLotCode(source.getLotCode());
        target.setQuantity(source.getQuantity());
        target.setBlocked(source.getBlocked());
        target.setSold(source.getSold());
        target.setUnitPrice(source.getUnitPrice());
        target.setCurrency(source.getCurrency());
        target.setCountry(source.getCountry());
        target.setActive(source.isActive());
        target.setPurchasedAt(parseDateTime(source.getPurchasedAt()));
        target.setMfgDate(parseDateTime(source.getMfgDate()));
        target.setExpiryAt(parseDateTime(source.getExpiryAt()));

        validateEntity(target);
        return target;
    }

    public List<ProductLotEntity> toEntityList(List<ProductLot> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public ProductLot toDomain(ProductLotEntity source) {
        if (source == null) return null;

        ProductLot target = ProductLot.builder().build();
        target.setId(source.getId());  // Added id mapping
        target.setProductId(source.getProduct().getId());
        target.setLotCode(source.getLotCode());
        target.setQuantity(source.getQuantity());
        target.setBlocked(source.getBlocked());
        target.setSold(source.getSold());
        target.setUnitPrice(source.getUnitPrice());
        target.setCurrency(source.getCurrency());
        target.setCountry(source.getCountry());
        target.setActive(source.isActive());
        target.setPurchasedAt(formatDateTime(source.getPurchasedAt()));
        target.setMfgDate(formatDateTime(source.getMfgDate()));
        target.setExpiryAt(formatDateTime(source.getExpiryAt()));

        return target;
    }

    public List<ProductLot> toDomainList(List<ProductLotEntity> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private void validateEntity(ProductLotEntity entity) {
        if (entity.getProduct() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (entity.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (entity.getBlocked() < 0) {
            throw new IllegalArgumentException("Blocked quantity cannot be negative");
        }
        if (entity.getSold() < 0) {
            throw new IllegalArgumentException("Sold quantity cannot be negative");
        }
        if (entity.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (entity.getSold() > entity.getQuantity()) {
            throw new IllegalArgumentException("Sold quantity cannot exceed total quantity");
        }
        if (entity.getBlocked() > (entity.getQuantity() - entity.getSold())) {
            throw new IllegalArgumentException("Blocked quantity cannot exceed available quantity");
        }
    }
}
