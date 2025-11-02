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

    @Autowired
    CurrencyEntityMapper currencyEntityMapper;

    public StoreItemPrice toDomain(StoreItemPriceEntity entity){
        if (entity == null) {
            return null;
        }
        StoreItemPrice.StoreItemPriceBuilder builder = StoreItemPrice.builder()
                .id(entity.getId())
                .price(entity.getPrice());

        if(  entity.getProductLot()!=null &&  entity.getProductLot().getProduct()!=null ) {
            builder.productLotId(entity.getProductLot().getId());
        }

        if(  entity.getCurrency()!=null)
            builder.currency(entity.getCurrency());

        if(  entity.getService()!=null)
            builder.serviceId(entity.getService().getId());
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

        if (store.getProductLotId() != null) {
            builder.productLot(ProductLotEntity.builder().id(store.getProductLotId()).build());
        }
        if (store.getServiceId() != null) {
            builder.service(StoreOfferedServiceEntity.builder().id(store.getServiceId()).build());
        }
        if (store.getCurrency() != null) {
            builder.currency(store.getCurrency());
        }
        return builder.build();

    }
}
