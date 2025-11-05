package biz.craftline.server.feature.businessstore.infra.mapper;

import biz.craftline.server.feature.businessstore.domain.model.StoreItemPrice;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreItemPriceEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import org.springframework.beans.factory.annotation.Autowired;
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
                .price(entity.getPrice());

        builder.itemType(entity.getItemType());
        builder.itemId(entity.getItemId());

        if(  entity.getCurrency()!=null)
            builder.currency(entity.getCurrency());

        if(  entity.getService()!=null)
            builder.itemId(entity.getService().getId());
        return builder.build();
    }

    public StoreItemPriceEntity toEntity(StoreItemPrice store){
        if (store == null) {
            return null;
        }
        StoreItemPriceEntity.StoreItemPriceEntityBuilder builder = StoreItemPriceEntity.builder()
                .price(store.getPrice())
                .createdBy(0L) // Default value, should be set by service layer
                .validFrom(LocalDateTime.now());

        if( store.getItemType()!=null && store.getItemId()!=null){
            if(store.getItemType() == 1 ){
                builder.productLot(ProductLotEntity.builder().id(store.getItemId()).build());
            }else if(store.getItemType() == 2 ){
                builder.service(StoreOfferedServiceEntity.builder().id(store.getItemId()).build());
            }
        }

        builder.itemType(store.getItemType());
        builder.itemId(store.getItemId());

        if (store.getCurrency() != null) {
            builder.currency(store.getCurrency());
        }
        return builder.build();

    }
}
