package biz.craftline.server.feature.inventorymanagement.api.mapper;

import biz.craftline.server.feature.inventorymanagement.api.dto.ProductLotTransactionDTO;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLotTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductLotTransactionDTOMapper {

    public ProductLotTransactionDTO toDTO(ProductLotTransaction source) {
        if (source == null) return null;

        ProductLotTransactionDTO target =  ProductLotTransactionDTO.builder()
                .id(source.getId())
                .productLotId(source.getProductLotId())
                .type(source.getType())
                .quantityChange( source.getQuantityChange() )
                .beforeQuantity(source.getBeforeQuantity())
                .unitPrice(source.getUnitPrice())
                .reason(source.getReason())
                .referenceId(source.getReferenceId())
                .correlationId(source.getCorrelationId())
                .performedBy(source.getPerformedBy())
                .build();

        return target;
    }

    public List<ProductLotTransactionDTO> toDTOList(List<ProductLotTransaction> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductLotTransaction toDomain(ProductLotTransactionDTO source) {
        if (source == null) return null;

        ProductLotTransaction target =  ProductLotTransaction.builder()
                .id(source.getId())
                .productLotId(source.getProductLotId())
                .type(source.getType())
                .quantityChange( source.getQuantityChange() )
                .beforeQuantity(source.getBeforeQuantity())
                .unitPrice(source.getUnitPrice())
                .reason(source.getReason())
                .referenceId(source.getReferenceId())
                .correlationId(source.getCorrelationId())
                .performedBy(source.getPerformedBy())
                .build();

        return target;
    }

    public List<ProductLotTransaction> toDomainList(List<ProductLotTransactionDTO> sources) {
        if (sources == null) return null;
        return sources.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

}
