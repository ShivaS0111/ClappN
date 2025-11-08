package biz.craftline.server.feature.inventorymanagement.infra.mapper;

import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLotTransaction;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotTransactionEntity;
import biz.craftline.server.feature.inventorymanagement.infra.repository.ProductLotRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductLotTransactionEntityMapper {

    @Autowired
    ProductLotRepository productLotRepository;

    public ProductLotTransactionEntity toEntity(ProductLotTransaction source) {
        if (source == null) return null;

        ProductLotEntity productLotEntity = productLotRepository.findById(source.getProductLotId())
                .orElseThrow(() -> new RuntimeException("Lot found by id:" + source.getProductLotId()));

        ProductLotTransactionEntity target = ProductLotTransactionEntity.builder()
                .id(source.getId())
                .productLot(productLotEntity)
                .type(source.getType())
                .quantityChange(source.getQuantityChange())
                .beforeQuantity(source.getBeforeQuantity())
                .unitPrice(source.getUnitPrice())
                .reason(source.getReason())
                .referenceId(source.getReferenceId())
                .correlationId(source.getCorrelationId())
                .performedBy(source.getPerformedBy())
                .build();

        return target;
    }

    public List<ProductLotTransactionEntity> toDTOList(List<ProductLotTransaction> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public ProductLotTransaction toDomain(ProductLotTransactionEntity source) {
        if (source == null) return null;

        ProductLotTransaction target = ProductLotTransaction.builder()
                .id(source.getId())
                .productLotId( source.getProductLot()!=null ? source.getProductLot().getId() : null )
                .type(source.getType())
                .quantityChange(source.getQuantityChange())
                .beforeQuantity(source.getBeforeQuantity())
                .unitPrice(source.getUnitPrice())
                .reason(source.getReason())
                .referenceId(source.getReferenceId())
                .correlationId(source.getCorrelationId())
                .performedBy(source.getPerformedBy())
                .build();

        return target;
    }

    public List<ProductLotTransaction> toDomainList(List<ProductLotTransactionEntity> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

}
