package biz.craftline.server.feature.inventorymanagement.api.controller;

import biz.craftline.server.feature.inventorymanagement.api.dto.StoreInventoryDTO;
import biz.craftline.server.feature.inventorymanagement.api.mapper.StoreInventoryDTOMapper;
import biz.craftline.server.feature.inventorymanagement.domain.model.StoreInventory;
import biz.craftline.server.feature.inventorymanagement.domain.service.StoreInventoryService;
import biz.craftline.server.util.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-inventory")
@RequiredArgsConstructor
public class StoreInventoryController {


    private final StoreInventoryService storeInventoryService;
    private final StoreInventoryDTOMapper storeInventoryDTOMapper;


    @PostMapping("/{storeId}/{productId}/add")
    public ResponseEntity<APIResponse<StoreInventoryDTO>> addStock(@PathVariable Long storeId, @PathVariable Long productId,
                                                                   @RequestParam int quantity,
                                                                   @RequestParam String referenceType,
                                                                   @RequestParam String referenceId,
                                                                   @RequestParam(required = false) String reason) {
        StoreInventory inventory = storeInventoryService.addStock(storeId, productId, quantity, referenceType, referenceId, reason);
        return APIResponse.ok(storeInventoryDTOMapper.toDomain(inventory));
    }


    @PostMapping("/{storeId}/{productId}/sell")
    public ResponseEntity<APIResponse<StoreInventoryDTO>> sellStock(@PathVariable Long storeId, @PathVariable Long productId,
                                                                    @RequestParam int quantity,
                                                                    @RequestParam String referenceType,
                                                                    @RequestParam String referenceId,
                                                                    @RequestParam(required = false) String reason) {
        return APIResponse.ok(storeInventoryDTOMapper.toDomain(storeInventoryService.adjustForSale(storeId, productId, quantity, referenceType, referenceId, reason)));
    }
}