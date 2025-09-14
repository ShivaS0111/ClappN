package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.DeliveryInfoDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.DeliveryInfoDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.DeliveryInfoService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/delivery-info")
public class DeliveryInfoController {
    private final DeliveryInfoService deliveryInfoService;

    public DeliveryInfoController(DeliveryInfoService deliveryInfoService) {
        this.deliveryInfoService = deliveryInfoService;
    }

    @GetMapping
    public List<DeliveryInfoDTO> getAllDeliveryInfo() {
        List<DeliveryInfo> infos = deliveryInfoService.getAllDeliveryInfo();
        List<DeliveryInfoDTO> dtos = new ArrayList<>();
        for (DeliveryInfo info : infos) {
            dtos.add(DeliveryInfoDTOMapper.toDTO(info));
        }
        return dtos;
    }

    @GetMapping("/{id}")
    public DeliveryInfoDTO getDeliveryInfo(@PathVariable Long id) {
        DeliveryInfo info = deliveryInfoService.getDeliveryInfo(id);
        return info != null ? DeliveryInfoDTOMapper.toDTO(info) : null;
    }

    @PostMapping
    public DeliveryInfoDTO addDeliveryInfo(@RequestBody DeliveryInfoDTO dto) {
        DeliveryInfo info = DeliveryInfoDTOMapper.fromDTO(dto);
        DeliveryInfo saved = deliveryInfoService.addDeliveryInfo(info);
        return DeliveryInfoDTOMapper.toDTO(saved);
    }

    @PutMapping("/{id}")
    public DeliveryInfoDTO updateDeliveryInfo(@PathVariable Long id, @RequestBody DeliveryInfoDTO dto) {
        DeliveryInfo info = DeliveryInfoDTOMapper.fromDTO(dto);
        DeliveryInfo updated = deliveryInfoService.updateDeliveryInfo(id, info);
        return updated != null ? DeliveryInfoDTOMapper.toDTO(updated) : null;
    }

    @DeleteMapping("/{id}")
    public void deleteDeliveryInfo(@PathVariable Long id) {
        deliveryInfoService.deleteDeliveryInfo(id);
    }
}

