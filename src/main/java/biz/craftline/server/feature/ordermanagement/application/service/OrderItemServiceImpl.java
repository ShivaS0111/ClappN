package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.domain.model.OrderItem;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderItemService;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderItemEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository repository;
    private final SecurityContextService securityContextService;

    @Autowired
    public OrderItemServiceImpl(OrderItemRepository repository, SecurityContextService securityContextService) {
        this.repository = repository;
        this.securityContextService = securityContextService;
    }

    @Override
    public List<OrderItem> getAllOrderItems() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        return repository.findAll().stream()
                .filter(entity -> isAccessible(entity, accessibleStoreIds))
                .map(OrderItemEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItem getOrderItem(Long id) {
        return repository.findById(id)
                .filter(entity -> {
                    validateAccess(entity);
                    return true;
                })
                .map(OrderItemEntityMapper::toModel)
                .orElse(null);
    }

    @Override
    public OrderItem addOrderItem(OrderItem orderItem) {
        OrderItemEntity entity = OrderItemEntityMapper.toEntity(orderItem);
        if (entity.getOrder() != null) {
            validateAccess(entity);
        }
        OrderItemEntity saved = repository.save(entity);
        return OrderItemEntityMapper.toModel(saved);
    }

    @Override
    public OrderItem updateOrderItem(Long id, OrderItem orderItem) {
        OrderItemEntity existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        validateAccess(existing);
        OrderItemEntity entity = OrderItemEntityMapper.toEntity(orderItem);
        entity.setId(id);
        OrderItemEntity saved = repository.save(entity);
        return OrderItemEntityMapper.toModel(saved);
    }

    @Override
    public void deleteOrderItem(Long id) {
        repository.findById(id).ifPresent(entity -> {
            validateAccess(entity);
            repository.deleteById(id);
        });
    }

    private void validateAccess(OrderItemEntity entity) {
        if (entity.getOrder() != null && entity.getOrder().getStoreId() != null) {
            securityContextService.validateStoreAccess(entity.getOrder().getStoreId());
        }
    }

    private boolean isAccessible(OrderItemEntity entity, List<Long> accessibleStoreIds) {
        if (accessibleStoreIds == null) {
            return true; // SYSTEM_ADMIN
        }
        if (entity.getOrder() == null || entity.getOrder().getStoreId() == null) {
            return false;
        }
        return accessibleStoreIds.contains(entity.getOrder().getStoreId());
    }
}
