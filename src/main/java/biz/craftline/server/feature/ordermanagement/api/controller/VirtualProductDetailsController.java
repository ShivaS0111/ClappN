package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.ordermanagement.api.dto.VirtualProductDetailsDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.VirtualProductDetailsDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.VirtualProductDetails;
import biz.craftline.server.feature.ordermanagement.domain.service.VirtualProductDetailsService;
import biz.craftline.server.util.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/virtual-product-details")
public class VirtualProductDetailsController {
    private final VirtualProductDetailsService virtualProductDetailsService;

    public VirtualProductDetailsController(VirtualProductDetailsService virtualProductDetailsService) {
        this.virtualProductDetailsService = virtualProductDetailsService;
    }

    @GetMapping
    @RequirePermission("product.read")
    public ResponseEntity<APIResponse<List<VirtualProductDetailsDTO>>> getAllVirtualProductDetails() {
        List<VirtualProductDetails> details = virtualProductDetailsService.getAllVirtualProductDetails();
        List<VirtualProductDetailsDTO> dtos = new ArrayList<>();
        for (VirtualProductDetails detail : details) {
            dtos.add(VirtualProductDetailsDTOMapper.toDTO(detail));
        }
        return APIResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    @RequirePermission("product.read")
    public ResponseEntity<APIResponse<VirtualProductDetailsDTO>> getVirtualProductDetails(@PathVariable Long id) {
        VirtualProductDetails detail = virtualProductDetailsService.getVirtualProductDetails(id);
        return APIResponse.ok(detail != null ? VirtualProductDetailsDTOMapper.toDTO(detail) : null);
    }

    @PostMapping
    @RequirePermission("product.create")
    public ResponseEntity<APIResponse<VirtualProductDetailsDTO>> addVirtualProductDetails(@RequestBody VirtualProductDetailsDTO dto) {
        VirtualProductDetails detail = VirtualProductDetailsDTOMapper.fromDTO(dto);
        VirtualProductDetails saved = virtualProductDetailsService.addVirtualProductDetails(detail);
        return APIResponse.ok(VirtualProductDetailsDTOMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    @RequirePermission("product.update")
    public ResponseEntity<APIResponse<VirtualProductDetailsDTO>> updateVirtualProductDetails(@PathVariable Long id, @RequestBody VirtualProductDetailsDTO dto) {
        VirtualProductDetails detail = VirtualProductDetailsDTOMapper.fromDTO(dto);
        VirtualProductDetails updated = virtualProductDetailsService.updateVirtualProductDetails(id, detail);
        return APIResponse.ok(updated != null ? VirtualProductDetailsDTOMapper.toDTO(updated) : null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("product.delete")
    public void deleteVirtualProductDetails(@PathVariable Long id) {
        virtualProductDetailsService.deleteVirtualProductDetails(id);
    }
}
