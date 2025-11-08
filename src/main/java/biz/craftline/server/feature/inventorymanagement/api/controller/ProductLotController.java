package biz.craftline.server.feature.inventorymanagement.api.controller;

import biz.craftline.server.feature.inventorymanagement.api.dto.ProductLotDTO;
import biz.craftline.server.feature.inventorymanagement.api.dto.ProductLotTransactionDTO;
import biz.craftline.server.feature.inventorymanagement.api.mapper.ProductLotDTOMapper;
import biz.craftline.server.feature.inventorymanagement.api.mapper.ProductLotTransactionDTOMapper;
import biz.craftline.server.feature.inventorymanagement.api.request.AddProductLotRequest;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLot;
import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLotTransaction;
import biz.craftline.server.feature.inventorymanagement.domain.service.ProductLotService;
import biz.craftline.server.feature.inventorymanagement.application.enums.TransactionType;
import biz.craftline.server.util.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-lots")
@RequiredArgsConstructor
public class ProductLotController {

    private final ProductLotService lotService;

    private final ProductLotDTOMapper productLotDTOMapper;
    private final ProductLotTransactionDTOMapper lotTransactionDTOMapper;

    @PostMapping
    public ResponseEntity<APIResponse<ProductLotDTO>> createLot(@RequestBody AddProductLotRequest lot) {
        ProductLot productLot = lotService.createLot(productLotDTOMapper.toDomain(lot));
        return APIResponse.ok(productLotDTOMapper.toDTO(productLot));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ProductLotDTO>> getLot(@PathVariable Long id) {
        ProductLot productLot = lotService.getLotById(id);
        return APIResponse.ok(productLotDTOMapper.toDTO(productLot));
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<ProductLotDTO>>> getAllActiveLots() {
        List<ProductLotDTO> lots = lotService.getAllActiveLots().stream().map(productLotDTOMapper::toDTO).toList();
        return APIResponse.ok(lots);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<ProductLotDTO>> updateLot(@PathVariable Long id,
                                                                @RequestBody ProductLotDTO updatedLot) {
        updatedLot.setId(id);
        ProductLot lot = lotService.updateLot(productLotDTOMapper.toDomain(updatedLot));
        return APIResponse.ok(productLotDTOMapper.toDTO(lot));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteLot(@PathVariable Long id) {
        return lotService.deleteLot(id) ?
                APIResponse.success("Success") :
                APIResponse.error("failed", HttpStatus.NO_CONTENT);
    }

    // 🔁 Transaction APIs
    @PostMapping("/{lotId}/transaction")
    public ResponseEntity<APIResponse<ProductLotTransactionDTO>> recordTransaction(
            @PathVariable Long lotId,
            @RequestParam TransactionType type,
            @RequestParam int quantity,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String referenceId,
            @RequestParam long performedBy
    ) {
        ProductLotTransaction tx = lotService.recordTransaction(lotId, type, quantity,
                reason, referenceId, performedBy);
        return APIResponse.ok(lotTransactionDTOMapper.toDTO(tx));
    }

    @GetMapping("/{lotId}/transactions")
    public ResponseEntity<APIResponse<List<ProductLotTransactionDTO>>> getTransactionsForLot(@PathVariable Long lotId) {
        List<ProductLotTransactionDTO> list = lotService.getTransactionsForLot(lotId).stream()
                .map(lotTransactionDTOMapper::toDTO).toList();
        return APIResponse.ok(list);
    }
}
