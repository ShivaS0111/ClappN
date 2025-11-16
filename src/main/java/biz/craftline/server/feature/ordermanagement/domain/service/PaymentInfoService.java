package biz.craftline.server.feature.ordermanagement.domain.service;

import biz.craftline.server.feature.paymentmanagement.domain.model.PaymentInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PaymentInfoService {
    List<PaymentInfo> getAllPaymentInfo();
    PaymentInfo getPaymentInfo(Long id);
    PaymentInfo addPaymentInfo(PaymentInfo paymentInfo);
    PaymentInfo updatePaymentInfo(Long id, PaymentInfo paymentInfo);
    void deletePaymentInfo(Long id);
}

