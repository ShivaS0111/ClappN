package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.feature.ordermanagement.domain.model.Order;
import biz.craftline.server.feature.ordermanagement.domain.service.OrderService;
import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.OrderEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;

    @Autowired
    public OrderServiceImpl(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Order> getAllOrders() {
        return repository.findAll().stream()
                .map(OrderEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Order getOrder(Long id) {
        return repository.findById(id)
                .map(OrderEntityMapper::toModel)
                .orElse(null);
    }

    @Override
    public Order placeOrder(Order order) {
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        OrderEntity saved = repository.save(entity);
        return OrderEntityMapper.toModel(saved);
    }

    @Override
    public Order updateOrder(Long id, Order order) {
        if (!repository.existsById(id)) return null;
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
            entity.setStatus("CANCELLED");
            repository.save(entity);
        });
    }

    @Override
    public void completeOrder(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setStatus("COMPLETED");
            repository.save(entity);
        });
    }
}
