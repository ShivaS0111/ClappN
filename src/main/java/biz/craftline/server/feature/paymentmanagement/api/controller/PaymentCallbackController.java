package biz.craftline.server.feature.paymentmanagement.api.controller;


import biz.craftline.server.feature.paymentmanagement.infra.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentCallbackController {
    private final PaymentTransactionRepository txRepo;

    @GetMapping("/callback")
    public String paymentCallback(
            @RequestParam Long orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sessionId,
            Model model) {

        log.info("Payment callback: orderId={}, status={}, sessionId={}", orderId, status, sessionId);

        // Add data to model for template
        model.addAttribute("orderId", orderId);
        model.addAttribute("status", status);
        model.addAttribute("sessionId", sessionId);

        // Webhook will update transaction asynchronously
        // Frontend can poll /api/payments/status/{providerPaymentId} for real status
        return "payment-callback";
    }
}