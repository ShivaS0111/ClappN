package biz.craftline.server.feature.ordermanagement.application.service;

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
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAllocationServiceImpl allocationService;

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

    public Order placeOrder1(Order order) {
        OrderEntity entity = OrderEntityMapper.toEntity(order);
        OrderEntity saved = repository.save(entity);
        return OrderEntityMapper.toModel(saved);
    }

    @Transactional
    public Order placeOrder(Order request) {
        // basic validation
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have items");
        }
        // persist order entity
        var entity = OrderEntityMapper.toEntity(request);
        entity.setStatus(OrderStatus.CREATED.toString());
        var saved = repository.save(entity);

        BigDecimal total = BigDecimal.ZERO;
        for (var it : request.getItems()) {
            var itemEnt = OrderItemEntityMapper.toEntity(it);
            itemEnt.setOrder(saved);
            var savedItem = orderItemRepository.save(itemEnt);
            total = total.add(BigDecimal.valueOf(it.getPrice() * it.getQuantity()));

            if(it.getItemType() == 0 ) {
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
            initiateRefund(entity.getTotalAmount());
        });
    }

    private void initiateRefund(BigDecimal totalAmount) {
    }

    @Override
    public void completeOrder(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setStatus("COMPLETED");
            repository.save(entity);
        });
    }
}
