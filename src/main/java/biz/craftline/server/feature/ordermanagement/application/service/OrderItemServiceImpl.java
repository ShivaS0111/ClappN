package biz.craftline.server.feature.ordermanagement.application.service;

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

    @Autowired
    public OrderItemServiceImpl(OrderItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<OrderItem> getAllOrderItems() {
        return repository.findAll().stream()
                .map(OrderItemEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItem getOrderItem(Long id) {
        return repository.findById(id)
                .map(OrderItemEntityMapper::toModel)
                .orElse(null);
    }

    @Override
    public OrderItem addOrderItem(OrderItem orderItem) {
        OrderItemEntity entity = OrderItemEntityMapper.toEntity(orderItem);
        OrderItemEntity saved = repository.save(entity);
        return OrderItemEntityMapper.toModel(saved);
    }

    @Override
    public OrderItem updateOrderItem(Long id, OrderItem orderItem) {
        if (!repository.existsById(id)) return null;
        OrderItemEntity entity = OrderItemEntityMapper.toEntity(orderItem);
        entity.setId(id);
        OrderItemEntity saved = repository.save(entity);
        return OrderItemEntityMapper.toModel(saved);
    }

    @Override
    public void deleteOrderItem(Long id) {
        repository.deleteById(id);
    }
}

