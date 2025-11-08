package biz.craftline.server.feature.inventorymanagement.infra.mapper;

import biz.craftline.server.feature.inventorymanagement.domain.model.StoreInventory;
import biz.craftline.server.feature.inventorymanagement.infra.entity.StoreInventoryEntity;
import org.springframework.stereotype.Component;

@Component
public class StoreInventoryEntityMapper {

    public StoreInventory toDomain(StoreInventoryEntity source){
        return StoreInventory.builder()
                .id(source.getId())
                .storeId(source.getId())
                .productId(source.getId())
                .totalQuantity(source.getTotalQuantity())
                .available(source.getAvailable())
                .blocked(source.getBlocked())
                .sold(source.getSold())
                .damaged(source.getDamaged())
                .build();
    }

    public  StoreInventoryEntity toDomain(StoreInventory source){
        return StoreInventoryEntity.builder()
                .id(source.getId())
                .storeId(source.getId())
                .productId(source.getId())
                .totalQuantity(source.getTotalQuantity())
                .available(source.getAvailable())
                .blocked(source.getBlocked())
                .sold(source.getSold())
                .damaged(source.getDamaged())
                .build();
    }
}
