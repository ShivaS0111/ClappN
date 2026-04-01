package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class StoreItemPriceEntityMapper {

    public StoreItemPrice toDomain(StoreItemPriceEntity entity){
        if (entity == null) {
            return null;
        }
        StoreItemPrice.StoreItemPriceBuilder builder = StoreItemPrice.builder()
                .id(entity.getId())
                .price(entity.getPrice())
                .status(entity.getStatus());

        builder.itemType(entity.getItemType());
        builder.itemId(entity.getItemId());

        if(  entity.getCurrency()!=null)
            builder.currency(entity.getCurrency());

        return builder.build();
    }

    public StoreItemPriceEntity toEntity(StoreItemPrice store){
        if (store == null) {
            return null;
        }
        StoreItemPriceEntity.StoreItemPriceEntityBuilder builder = StoreItemPriceEntity.builder()
                .price(store.getPrice())
                .createdBy(0L) // Default value, should be set by service layer
                .validFrom(LocalDateTime.now())
                .status(store.getStatus() > 0 ? store.getStatus() : 1);

        builder.itemType(store.getItemType());
        builder.itemId(store.getItemId());

        if (store.getCurrency() != null) {
            builder.currency(store.getCurrency());
        }
        return builder.build();

    }
}
