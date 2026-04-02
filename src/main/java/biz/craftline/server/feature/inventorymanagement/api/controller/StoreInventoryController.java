package biz.craftline.server.feature.inventorymanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.inventorymanagement.api.dto.StoreInventoryDTO;
import biz.craftline.server.feature.inventorymanagement.api.mapper.StoreInventoryDTOMapper;
import biz.craftline.server.feature.inventorymanagement.domain.model.StoreInventory;
import biz.craftline.server.feature.inventorymanagement.domain.service.StoreInventoryService;
import biz.craftline.server.util.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/store-inventory")
@RequiredArgsConstructor
public class StoreInventoryController {


    private final StoreInventoryService storeInventoryService;
    private final StoreInventoryDTOMapper storeInventoryDTOMapper;

    /**
     * Get all inventory items for a specific store.
     */
    @GetMapping("/{storeId}")
    @RequirePermission("inventory.read")
    public ResponseEntity<APIResponse<List<StoreInventoryDTO>>> getInventoryByStore(@PathVariable Long storeId) {
        List<StoreInventory> inventoryList = storeInventoryService.findByStoreId(storeId);
        List<StoreInventoryDTO> dtoList = inventoryList.stream()
                .map(storeInventoryDTOMapper::toDomain)
                .collect(Collectors.toList());
        return APIResponse.success(dtoList, "Store inventory retrieved successfully");
    }

    @PostMapping("/{storeId}/{productId}/add")
    @RequirePermission("inventory.create")
    public ResponseEntity<APIResponse<StoreInventoryDTO>> addStock(@PathVariable Long storeId, @PathVariable Long productId,
                                                                   @RequestParam int quantity,
                                                                   @RequestParam String referenceType,
                                                                   @RequestParam String referenceId,
                                                                   @RequestParam(required = false) String reason) {
        StoreInventory inventory = storeInventoryService.addStock(storeId, productId, quantity, referenceType, referenceId, reason);
        return APIResponse.ok(storeInventoryDTOMapper.toDomain(inventory));
    }


    @PostMapping("/{storeId}/{productId}/sell")
    @RequirePermission("inventory.update")
    public ResponseEntity<APIResponse<StoreInventoryDTO>> sellStock(@PathVariable Long storeId, @PathVariable Long productId,
                                                                    @RequestParam int quantity,
                                                                    @RequestParam String referenceType,
                                                                    @RequestParam String referenceId,
                                                                    @RequestParam(required = false) String reason) {
        return APIResponse.ok(storeInventoryDTOMapper.toDomain(storeInventoryService.adjustForSale(storeId, productId, quantity, referenceType, referenceId, reason)));
    }
}