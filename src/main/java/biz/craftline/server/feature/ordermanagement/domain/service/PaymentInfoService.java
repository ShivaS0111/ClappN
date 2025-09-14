package biz.craftline.server.feature.ordermanagement.domain.service;

import biz.craftline.server.feature.ordermanagement.domain.model.PaymentInfo;
import java.util.List;

public interface PaymentInfoService {
    List<PaymentInfo> getAllPaymentInfo();
    PaymentInfo getPaymentInfo(Long id);
    PaymentInfo addPaymentInfo(PaymentInfo paymentInfo);
    PaymentInfo updatePaymentInfo(Long id, PaymentInfo paymentInfo);
    void deletePaymentInfo(Long id);
}

