package biz.craftline.server.feature.paymentmanagement.api.controller;

import biz.craftline.server.feature.paymentmanagement.api.dto.PaymentInfoDTO;
import biz.craftline.server.feature.paymentmanagement.api.mapper.PaymentInfoDTOMapper;
import biz.craftline.server.feature.paymentmanagement.api.request.InitiatePaymentRequest;
import biz.craftline.server.feature.paymentmanagement.api.response.InitiatePaymentResponse;
import biz.craftline.server.feature.paymentmanagement.domain.model.PaymentInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.PaymentInfoService;
import biz.craftline.server.feature.paymentmanagement.domain.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/*

@AllArgsConstructor
@RestController("orderPaymentInfoController")
@RequestMapping("/api/payment-info")
public class PaymentInfoController {

    private final PaymentInfoService paymentInfoService;


    @GetMapping
    public List<PaymentInfoDTO> getAllPaymentInfo() {
        List<PaymentInfo> infos = paymentInfoService.getAllPaymentInfo();
        List<PaymentInfoDTO> paymentDTOs = new ArrayList<>();
        for (PaymentInfo info : infos) {
            paymentDTOs.add(PaymentInfoDTOMapper.toDTO(info));
        }
        return paymentDTOs;
    }

    @GetMapping("/{id}")
    public PaymentInfoDTO getPaymentInfo(@PathVariable Long id) {
        PaymentInfo info = paymentInfoService.getPaymentInfo(id);
        return info != null ? PaymentInfoDTOMapper.toDTO(info) : null;
    }

    @PostMapping
    public PaymentInfoDTO addPaymentInfo(@RequestBody PaymentInfoDTO dto) {
        PaymentInfo info = PaymentInfoDTOMapper.fromDTO(dto);
        PaymentInfo saved = paymentInfoService.addPaymentInfo(info);
        return PaymentInfoDTOMapper.toDTO(saved);
    }

    @PutMapping("/{id}")
    public PaymentInfoDTO updatePaymentInfo(@PathVariable Long id, @RequestBody PaymentInfoDTO dto) {
        PaymentInfo info = PaymentInfoDTOMapper.fromDTO(dto);
        PaymentInfo updated = paymentInfoService.updatePaymentInfo(id, info);
        return updated != null ? PaymentInfoDTOMapper.toDTO(updated) : null;
    }

    @DeleteMapping("/{id}")
    public void deletePaymentInfo(@PathVariable Long id) {
        paymentInfoService.deletePaymentInfo(id);
    }

}
*/
