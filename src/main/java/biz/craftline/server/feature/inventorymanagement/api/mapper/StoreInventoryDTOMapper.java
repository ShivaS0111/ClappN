package biz.craftline.server.feature.inventorymanagement.api.mapper;

import biz.craftline.server.feature.inventorymanagement.api.dto.StoreInventoryDTO;
import biz.craftline.server.feature.inventorymanagement.domain.model.StoreInventory;
import org.springframework.stereotype.Component;

@Component
public class StoreInventoryDTOMapper {

    public StoreInventoryDTO toDomain(StoreInventory source){
        return StoreInventoryDTO.builder()
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

    public StoreInventory toDomain(StoreInventoryDTO source){
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
}
