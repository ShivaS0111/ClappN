package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.PaymentInfoDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.PaymentInfoDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.PaymentInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.PaymentInfoService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/payment-info")
public class PaymentInfoController {
    private final PaymentInfoService paymentInfoService;

    public PaymentInfoController(PaymentInfoService paymentInfoService) {
        this.paymentInfoService = paymentInfoService;
    }

    @GetMapping
    public List<PaymentInfoDTO> getAllPaymentInfo() {
        List<PaymentInfo> infos = paymentInfoService.getAllPaymentInfo();
        List<PaymentInfoDTO> dtos = new ArrayList<>();
        for (PaymentInfo info : infos) {
            dtos.add(PaymentInfoDTOMapper.toDTO(info));
        }
        return dtos;
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

