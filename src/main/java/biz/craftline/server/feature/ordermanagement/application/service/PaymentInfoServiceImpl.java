package biz.craftline.server.feature.ordermanagement.application.service;

import biz.craftline.server.feature.ordermanagement.domain.model.PaymentInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.PaymentInfoService;
import biz.craftline.server.feature.ordermanagement.infra.entity.PaymentInfoEntity;
import biz.craftline.server.feature.ordermanagement.infra.mapper.PaymentInfoEntityMapper;
import biz.craftline.server.feature.ordermanagement.infra.repository.PaymentInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {
    private final PaymentInfoRepository repository;

    @Autowired
    public PaymentInfoServiceImpl(PaymentInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PaymentInfo> getAllPaymentInfo() {
        return repository.findAll().stream()
                .map(PaymentInfoEntityMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentInfo getPaymentInfo(Long id) {
        return repository.findById(id)
                .map(PaymentInfoEntityMapper::toModel)
                .orElse(null);
    }

    @Override
    public PaymentInfo addPaymentInfo(PaymentInfo paymentInfo) {
        PaymentInfoEntity entity = PaymentInfoEntityMapper.toEntity(paymentInfo);
        PaymentInfoEntity saved = repository.save(entity);
        return PaymentInfoEntityMapper.toModel(saved);
    }

    @Override
    public PaymentInfo updatePaymentInfo(Long id, PaymentInfo paymentInfo) {
        if (!repository.existsById(id)) return null;
        PaymentInfoEntity entity = PaymentInfoEntityMapper.toEntity(paymentInfo);
        entity.setId(id);
        PaymentInfoEntity saved = repository.save(entity);
        return PaymentInfoEntityMapper.toModel(saved);
    }

    @Override
    public void deletePaymentInfo(Long id) {
        repository.deleteById(id);
    }
}

