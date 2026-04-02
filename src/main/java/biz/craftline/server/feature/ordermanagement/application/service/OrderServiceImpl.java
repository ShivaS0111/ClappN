package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.application.enums.OrderItemStatus;
import biz.craftline.server.feature.ordermanagement.application.enums.OrderStatus;
import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderItemEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAllocationServiceImpl allocationService;
    private final SecurityContextService securityContextService;

    @Override
    public List<Order> getAllOrders() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            // SYSTEM_ADMIN — unrestricted
            return repository.findAll().stream()
                    .map(OrderEntityMapper::toModel)
                    .collect(Collectors.toList());
        }
        if (accessibleStoreIds.isEmpty()) {
            return List.of();
        }
        return repository.findByStoreIdIn(accessibleStoreIds).stream()
                .map(OrderEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrdersByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        return repository.findByStoreId(storeId).stream()
                .map(OrderEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = repository.findByCustomerId(customerId).stream()
                .map(OrderEntityMapper::toModel)
                .collect(Collectors.toList());
        // Filter by accessible stores
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            return orders; // SYSTEM_ADMIN
        }
        return orders.stream()
                .filter(o -> o.getStoreId() != null && accessibleStoreIds.contains(o.getStoreId()))
                .collect(Collectors.toList());
    }

    @Override
    public Order getOrder(Long id) {
        Order order = repository.findById(id)
                .map(OrderEntityMapper::toModel)
                .orElse(null);
        if (order != null) {
            securityContextService.validateStoreAccess(order.getStoreId());
        }
        return order;
    }

    public Order placeOrder1(Order order) {
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        OrderEntity saved = repository.save(entity);
        return OrderEntityMapper.toModel(saved);
    }

    @Transactional
    public Order placeOrder(Order request) {
        // Validate store access
        securityContextService.validateStoreAccess(request.getStoreId());
        // basic validation
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have items");
        }
        // persist order entity (without items first to avoid cascade issues)
        var entity = OrderEntityMapper.toEntity(request);
        entity.setItems(null); // detach items — we save them manually below
        entity.setStatus(OrderStatus.CREATED.toString());
        if (entity.getOrderDate() == null) {
            entity.setOrderDate(LocalDateTime.now());
        }
        if (entity.getTotalAmount() == null) {
            entity.setTotalAmount(BigDecimal.ZERO);
        }
        var saved = repository.save(entity);

        BigDecimal total = BigDecimal.ZERO;
        for (var it : request.getItems()) {
            var itemEnt = OrderItemEntityMapper.toEntity(it);
            itemEnt.setOrder(saved);
            // Default item status to PENDING for new orders (POS walk-in, etc.)
            if (itemEnt.getStatus() == null) {
                itemEnt.setStatus(OrderItemStatus.PENDING);
            }
            var savedItem = orderItemRepository.save(itemEnt);
            total = total.add(BigDecimal.valueOf(it.getPrice() * it.getQuantity()));

            // Allocate inventory for physical products (itemType 1 = PRODUCT)
            if(it.getItemType() != null && it.getItemType() == 1) {
                // allocate lots FEFO (partial allowed)
                allocationService.allocate( request.getStoreId(), it.getItemIId(), it.getQuantity(), savedItem.getId());
            }
        }

        saved.setTotalAmount(total);
        saved.setStatus(OrderStatus.BLOCKED.toString());
        repository.save(saved);
        initiatePayment(total);

        return OrderEntityMapper.toDomain(saved, orderItemRepository.findByOrder_Id(saved.getId()));
    }

    private void initiatePayment(BigDecimal total) {
    }

    private void confirmPayment(BigDecimal total) {
    }


    @Override
    public Order updateOrder(Long id, Order order) {
        OrderEntity existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        securityContextService.validateStoreAccess(existing.getStoreId());
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        entity.setId(id);
        OrderEntity saved = repository.save(entity);
        return OrderEntityMapper.toModel(saved);
    }

    @Override
    public void deleteOrder(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void cancelOrder(Long id) {
        repository.findById(id).ifPresent(entity -> {
            securityContextService.validateStoreAccess(entity.getStoreId());
            entity.setStatus("CANCELLED");
            repository.save(entity);
            initiateRefund(entity.getTotalAmount());
        });
    }

    private void initiateRefund(BigDecimal totalAmount) {
    }

    @Override
    public void completeOrder(Long id) {
        repository.findById(id).ifPresent(entity -> {
            securityContextService.validateStoreAccess(entity.getStoreId());
            entity.setStatus("COMPLETED");
            repository.save(entity);
        });
    }
}
