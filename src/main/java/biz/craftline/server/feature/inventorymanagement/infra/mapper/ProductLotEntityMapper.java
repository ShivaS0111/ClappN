package biz.craftline.server.feature.inventorymanagement.infra.mapper;

import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLot;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import biz.craftline.server.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ProductLotEntityMapper {

    private final DateUtil dateUtil;

    public ProductLotEntity toEntity(ProductLot source) {
        if (source == null) return null;

        if(source.getProductId()==null) throw  new RuntimeException("Product ID is required");

        ProductLotEntity target = new ProductLotEntity();
        target.setId(source.getId());
        target.setProductId(source.getProductId());
        target.setStoreId(source.getStoreId());
        target.setLotCode(source.getLotCode());
        target.setQuantity(source.getQuantity());
        target.setBlocked(source.getBlocked());
        target.setSold(source.getSold());
        target.setUnitPrice(source.getUnitPrice());
        target.setCurrency(source.getCurrency());
        //target.setCountry(source.getCountry());
        target.setActive(source.getActive());
        target.setPurchasedAt(dateUtil.parseDate(source.getPurchasedAt()));
        target.setMfgDate(dateUtil.parseDate(source.getMfgDate()));
        target.setExpiryAt(dateUtil.parseDate(source.getExpiryAt()));

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
        target.setId(source.getId());
        target.setProductId(source.getProductId());
        target.setStoreId(source.getStoreId());
        target.setLotCode(source.getLotCode());
        target.setQuantity(source.getQuantity());
        target.setBlocked(source.getBlocked());
        target.setSold(source.getSold());
        target.setUnitPrice(source.getUnitPrice());
        target.setCurrency(source.getCurrency());
        //target.setCountry(source.getCountry());
        target.setActive(source.getActive());
        target.setPurchasedAt(dateUtil.formatDateTime(source.getPurchasedAt()));
        target.setMfgDate(dateUtil.formatDateTime(source.getMfgDate()));
        target.setExpiryAt(dateUtil.formatDateTime(source.getExpiryAt()));

        return target;
    }

    public List<ProductLot> toDomainList(List<ProductLotEntity> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private void validateEntity(ProductLotEntity entity) {
        if (entity.getProductId() == null) {
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
