package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.service.ProductLotService;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotTransaction;
import biz.craftline.server.feature.businessstore.infra.repository.ProductLotRepository;
import biz.craftline.server.feature.businessstore.infra.repository.ProductLotTransactionRepository;
import biz.craftline.server.util.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductLotServiceImpl implements ProductLotService {

    private final ProductLotRepository lotRepository;
    private final ProductLotTransactionRepository transactionRepository;

    @Override
    public ProductLotEntity createLot(ProductLotEntity lot) {
        return lotRepository.save(lot);
    }

    @Override
    public Optional<ProductLotEntity> getLotById(Long id) {
        return lotRepository.findById(id);
    }

    @Override
    public List<ProductLotEntity> getAllActiveLots() {
        return lotRepository.findByActiveTrue();
    }

    @Override
    public ProductLotEntity updateLot(ProductLotEntity lot) {
        return lotRepository.save(lot);
    }

    @Override
    public boolean deleteLot(Long id) {
        if (lotRepository.existsById(id)) {
            lotRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public ProductLotTransaction recordTransaction(Long lotId, TransactionType type, int quantityChange, String reason, String referenceId, long performedBy) {
        ProductLotEntity lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found: " + lotId));

        int before = lot.getAvailable();
        int after;

        switch (type) {
            case BLOCK -> lot.setBlocked(lot.getBlocked() + quantityChange);
            case UNBLOCK -> lot.setBlocked(lot.getBlocked() - quantityChange);
            case SOLD -> lot.setSold(lot.getSold() + quantityChange);
            case RETURN -> lot.setSold(lot.getSold() - quantityChange);
            case ADJUST -> lot.setQuantity(lot.getQuantity() + quantityChange);
            default -> throw new IllegalArgumentException("Invalid transaction type");
        }

        after = lot.getAvailable();
        lotRepository.save(lot);

        ProductLotTransaction tx = ProductLotTransaction.builder()
                .productLot(lot)
                .type(type)
                .quantityChange(quantityChange)
                .beforeQuantity(before)
                .afterQuantity(after)
                .unitPrice(BigDecimal.valueOf(lot.getUnitPrice()))
                .reason(reason)
                .referenceId(referenceId)
                .performedBy(performedBy)
                .correlationId("CORR-" + System.currentTimeMillis())
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(tx);
    }

    @Override
    public List<ProductLotTransaction> getTransactionsForLot(Long lotId) {
        ProductLotEntity lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot not found"));
        return transactionRepository.findByProductLot(lot);
    }
}