package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.DeliveryInfoDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.DeliveryInfoDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.DeliveryInfoService;
import biz.craftline.server.util.APIResponse;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<APIResponse<List<DeliveryInfoDTO>>> getAllDeliveryInfo() {
        List<DeliveryInfo> infos = deliveryInfoService.getAllDeliveryInfo();
        List<DeliveryInfoDTO> dtos = new ArrayList<>();
        for (DeliveryInfo info : infos) {
            dtos.add(DeliveryInfoDTOMapper.toDTO(info));
        }
        return APIResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<DeliveryInfoDTO>> getDeliveryInfo(@PathVariable Long id) {
        DeliveryInfo info = deliveryInfoService.getDeliveryInfo(id);
        return APIResponse.ok(info != null ? DeliveryInfoDTOMapper.toDTO(info) : null);
    }

    @PostMapping
    public ResponseEntity<APIResponse<DeliveryInfoDTO>> addDeliveryInfo(@RequestBody DeliveryInfoDTO dto) {
        DeliveryInfo info = DeliveryInfoDTOMapper.fromDTO(dto);
        DeliveryInfo saved = deliveryInfoService.addDeliveryInfo(info);
        return APIResponse.ok(DeliveryInfoDTOMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<DeliveryInfoDTO>> updateDeliveryInfo(@PathVariable Long id, @RequestBody DeliveryInfoDTO dto) {
        DeliveryInfo info = DeliveryInfoDTOMapper.fromDTO(dto);
        DeliveryInfo updated = deliveryInfoService.updateDeliveryInfo(id, info);
        return APIResponse.ok(updated != null ? DeliveryInfoDTOMapper.toDTO(updated) : null);
    }

    @DeleteMapping("/{id}")
    public void deleteDeliveryInfo(@PathVariable Long id) {
        deliveryInfoService.deleteDeliveryInfo(id);
    }
}

