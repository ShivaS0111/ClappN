package biz.craftline.server.feature.paymentmanagement.domain;

import biz.craftline.server.feature.paymentmanagement.domain.provider.PaymentProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentProviderFactory {
    private final Map<String, PaymentProvider> providers = new HashMap<>();

    public PaymentProviderFactory(@Qualifier("stripeProvider") PaymentProvider stripe,
                                  @Qualifier("razorpayProvider") PaymentProvider razorpay) {
        providers.put("STRIPE", stripe);
        providers.put("RAZORPAY", razorpay);
    }

    public PaymentProvider providerFor(String gateway) {
        if (gateway == null) return null;
        return providers.get(gateway.toUpperCase());
    }
}