package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.ordermanagement.api.dto.DeliveryInfoDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.DeliveryInfoDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.DeliveryInfo;
import biz.craftline.server.feature.ordermanagement.domain.service.DeliveryInfoService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/delivery-info")
public class DeliveryInfoController {
    private final DeliveryInfoService deliveryInfoService;
    private final DeliveryInfoDTOMapper deliveryInfoDTOMapper;


    @GetMapping
    @RequirePermission("order.read")
    public ResponseEntity<APIResponse<List<DeliveryInfoDTO>>> getAllDeliveryInfo() {
        List<DeliveryInfo> infos = deliveryInfoService.getAllDeliveryInfo();
        List<DeliveryInfoDTO> dtos = new ArrayList<>();
        for (DeliveryInfo info : infos) {
            dtos.add(deliveryInfoDTOMapper.toDTO(info));
        }
        return APIResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    @RequirePermission("order.read")
    public ResponseEntity<APIResponse<DeliveryInfoDTO>> getDeliveryInfo(@PathVariable Long id) {
        DeliveryInfo info = deliveryInfoService.getDeliveryInfo(id);
        return APIResponse.ok(info != null ? deliveryInfoDTOMapper.toDTO(info) : null);
    }

    @PostMapping
    @RequirePermission("order.create")
    public ResponseEntity<APIResponse<DeliveryInfoDTO>> addDeliveryInfo(@RequestBody DeliveryInfoDTO dto) {
        DeliveryInfo info = deliveryInfoDTOMapper.fromDTO(dto);
        DeliveryInfo saved = deliveryInfoService.addDeliveryInfo(info);
        return APIResponse.ok(deliveryInfoDTOMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @RequirePermission("order.update")
    public ResponseEntity<APIResponse<DeliveryInfoDTO>> updateDeliveryInfo(@PathVariable Long id, @RequestBody DeliveryInfoDTO dto) {
        DeliveryInfo info = deliveryInfoDTOMapper.fromDTO(dto);
        DeliveryInfo updated = deliveryInfoService.updateDeliveryInfo(id, info);
        return APIResponse.ok(updated != null ? deliveryInfoDTOMapper.toDTO(updated) : null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("order.delete")
    public void deleteDeliveryInfo(@PathVariable Long id) {
        deliveryInfoService.deleteDeliveryInfo(id);
    }
}
