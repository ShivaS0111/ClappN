package biz.craftline.server.feature.paymentmanagement.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.paymentmanagement.domain.model.PaymentInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.PaymentInfoService;
import biz.craftline.server.feature.paymentmanagement.infra.entity.PaymentInfoEntity;
import biz.craftline.server.feature.paymentmanagement.infra.mapper.PaymentInfoEntityMapper;
import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Standalone PaymentInfo CRUD has no store FK — restrict direct access to SYSTEM_ADMIN.
 * Normal flows attach payment info via order APIs.
 */
@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {
    private final PaymentInfoRepository repository;
    private final SecurityContextService securityContextService;

    @Autowired
    public PaymentInfoServiceImpl(PaymentInfoRepository repository,
                                  SecurityContextService securityContextService) {
        this.repository = repository;
        this.securityContextService = securityContextService;
    }

    private void requireAdmin() {
        if (!securityContextService.isSystemAdmin()) {
            throw new AccessDeniedException(
                    "Direct PaymentInfo access requires SYSTEM_ADMIN; use order APIs");
        }
    }

    @Override
    public List<PaymentInfo> getAllPaymentInfo() {
        requireAdmin();
        return repository.findAll().stream()
                .map(PaymentInfoEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentInfo getPaymentInfo(Long id) {
        requireAdmin();
        return repository.findById(id)
                .map(PaymentInfoEntityMapper::toModel)
                .orElse(null);
    }

    @Override
    public PaymentInfo addPaymentInfo(PaymentInfo paymentInfo) {
        requireAdmin();
        PaymentInfoEntity entity = PaymentInfoEntityMapper.toEntity(paymentInfo);
        PaymentInfoEntity saved = repository.save(entity);
        return PaymentInfoEntityMapper.toModel(saved);
    }

    @Override
    public PaymentInfo updatePaymentInfo(Long id, PaymentInfo paymentInfo) {
        requireAdmin();
        if (!repository.existsById(id)) return null;
        PaymentInfoEntity entity = PaymentInfoEntityMapper.toEntity(paymentInfo);
        entity.setId(id);
        PaymentInfoEntity saved = repository.save(entity);
        return PaymentInfoEntityMapper.toModel(saved);
    }

    @Override
    public void deletePaymentInfo(Long id) {
        requireAdmin();
        repository.deleteById(id);
    }
}
