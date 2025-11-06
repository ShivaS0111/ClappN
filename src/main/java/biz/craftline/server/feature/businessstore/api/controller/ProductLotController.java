package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.domain.service.ProductLotService;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotTransaction;
import biz.craftline.server.util.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-lots")
@RequiredArgsConstructor
public class ProductLotController {

    private final ProductLotService lotService;

    @PostMapping
    public ResponseEntity<ProductLotEntity> createLot(@RequestBody ProductLotEntity lot) {
        return ResponseEntity.ok(lotService.createLot(lot));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductLotEntity> getLot(@PathVariable Long id) {
        return lotService.getLotById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ProductLotEntity>> getAllActiveLots() {
        return ResponseEntity.ok(lotService.getAllActiveLots());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductLotEntity> updateLot(@PathVariable Long id, @RequestBody ProductLotEntity updatedLot) {
        updatedLot.setId(id);
        return ResponseEntity.ok(lotService.updateLot(updatedLot));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id) {
        return lotService.deleteLot(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // 🔁 Transaction APIs
    @PostMapping("/{lotId}/transaction")
    public ResponseEntity<ProductLotTransaction> recordTransaction(
            @PathVariable Long lotId,
            @RequestParam TransactionType type,
            @RequestParam int quantity,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String referenceId,
            @RequestParam long performedBy
    ) {
        ProductLotTransaction tx = lotService.recordTransaction(lotId, type, quantity, reason, referenceId, performedBy);
        return ResponseEntity.ok(tx);
    }

    @GetMapping("/{lotId}/transactions")
    public ResponseEntity<List<ProductLotTransaction>> getTransactionsForLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(lotService.getTransactionsForLot(lotId));
    }
}
