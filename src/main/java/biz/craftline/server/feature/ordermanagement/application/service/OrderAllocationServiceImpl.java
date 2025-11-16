package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.feature.inventorymanagement.domain.model.ProductLot;
import biz.craftline.server.feature.inventorymanagement.domain.service.ProductLotService;
import biz.craftline.server.feature.inventorymanagement.domain.service.StoreInventoryService;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderAllocatedLot;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderAllocatedLotEntity;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderAllocatedLotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
@Service
public class OrderAllocationServiceImpl {

    private final ProductLotService productLotService;
    private final StoreInventoryService storeInventoryService;
    private final OrderAllocatedLotRepository allocatedLotRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LocalDate parseOrMax(String date) {
        return date == null ? LocalDate.MAX : LocalDate.parse(date, DATE_FORMATTER);
    }

    /**
     * FEFO allocation: allocate across lots by expiry ASC (partial allowed)
     */
    @Transactional
    public List<OrderAllocatedLot> allocate(Long storeId, Long productId, int quantity, Long orderItemId) {
        List<ProductLot> lots = productLotService.findByStoreIdAndProductIdAndActiveTrue(storeId, productId);

        // sort by expiry (nulls last)
        lots.sort(Comparator.comparing(
                l -> parseOrMax(l.getExpiryAt())
        ));

        int remaining = quantity;
        List<OrderAllocatedLot> allocations = new ArrayList<>();

        for (var lot : lots) {
            if (remaining <= 0) break;
            int available = lot.getAvailable();
            if (available <= 0) continue;
            int allocate = Math.min(available, remaining);

            // create product lot transaction - BLOCK (delegated to productLotRepository/service)
            productLotService.productLotBlock(lot.getId(), allocate, "ORDER_ALLOC", orderItemId);

            // update store inventory block
            storeInventoryService.adjustBlocked(storeId, productId, allocate, "order-block");

            // persist allocation record
            OrderAllocatedLotEntity ent =
                    OrderAllocatedLotEntity.builder()
                            .orderItemId(orderItemId)
                            .productLotId(lot.getId())
                            .allocatedQuantity(allocate)
                            .build();
            allocatedLotRepository.save(ent);

            allocations.add(OrderAllocatedLot.builder()
                    .orderItemId(orderItemId)
                    .productLotId(lot.getId())
                    .allocatedQuantity(allocate)
                    .build());

            remaining -= allocate;
        }

        if (remaining > 0) {
            throw new RuntimeException("Insufficient stock for product " + productId);
        }

        return allocations;
    }
}
