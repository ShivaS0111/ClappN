package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.DeliveryInfoService;
import biz.craftline.server.feature.ordermanagement.infra.entity.DeliveryInfoEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.DeliveryInfoEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.DeliveryInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryInfoServiceImpl implements DeliveryInfoService {
    private final DeliveryInfoRepository repository;
    private final SecurityContextService securityContextService;

    @Autowired
    public DeliveryInfoServiceImpl(DeliveryInfoRepository repository,
                                   SecurityContextService securityContextService) {
        this.repository = repository;
        this.securityContextService = securityContextService;
    }

    @Override
    public List<DeliveryInfo> getAllDeliveryInfo() {
        // Delivery records are not independently store-scoped in schema;
        // only SYSTEM_ADMIN may list all. Others should access via order APIs.
        if (!securityContextService.isSystemAdmin()) {
            throw new AccessDeniedException("Listing all delivery info requires SYSTEM_ADMIN");
        }
        return repository.findAll().stream()
                .map(DeliveryInfoEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryInfo getDeliveryInfo(Long id) {
        if (!securityContextService.isSystemAdmin()) {
            throw new AccessDeniedException("Direct delivery info access requires SYSTEM_ADMIN; use order APIs");
        }
        return repository.findById(id)
                .map(DeliveryInfoEntityMapper::toModel)
                .orElse(null);
    }

    @Override
    public DeliveryInfo addDeliveryInfo(DeliveryInfo deliveryInfo) {
        DeliveryInfoEntity entity = DeliveryInfoEntityMapper.toEntity(deliveryInfo);
        DeliveryInfoEntity saved = repository.save(entity);
        return DeliveryInfoEntityMapper.toModel(saved);
    }

    @Override
    public DeliveryInfo updateDeliveryInfo(Long id, DeliveryInfo deliveryInfo) {
        if (!repository.existsById(id)) return null;
        DeliveryInfoEntity entity = DeliveryInfoEntityMapper.toEntity(deliveryInfo);
        entity.setId(id);
        DeliveryInfoEntity saved = repository.save(entity);
        return DeliveryInfoEntityMapper.toModel(saved);
    }

    @Override
    public void deleteDeliveryInfo(Long id) {
        if (!securityContextService.isSystemAdmin()) {
            throw new AccessDeniedException("Deleting delivery info requires SYSTEM_ADMIN");
        }
        repository.deleteById(id);
    }
}
