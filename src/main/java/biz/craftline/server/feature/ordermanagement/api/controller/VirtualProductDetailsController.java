package biz.craftline.server.feature.ordermanagement.api.controller;

import biz.craftline.server.feature.ordermanagement.api.dto.VirtualProductDetailsDTO;
import biz.craftline.server.feature.ordermanagement.api.mapper.VirtualProductDetailsDTOMapper;
import biz.craftline.server.feature.ordermanagement.domain.model.VirtualProductDetails;
import biz.craftline.server.feature.ordermanagement.domain.service.VirtualProductDetailsService;
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
    public List<VirtualProductDetailsDTO> getAllVirtualProductDetails() {
        List<VirtualProductDetails> details = virtualProductDetailsService.getAllVirtualProductDetails();
        List<VirtualProductDetailsDTO> dtos = new ArrayList<>();
        for (VirtualProductDetails detail : details) {
            dtos.add(VirtualProductDetailsDTOMapper.toDTO(detail));
        }
        return dtos;
    }

    @GetMapping("/{id}")
    public VirtualProductDetailsDTO getVirtualProductDetails(@PathVariable Long id) {
        VirtualProductDetails detail = virtualProductDetailsService.getVirtualProductDetails(id);
        return detail != null ? VirtualProductDetailsDTOMapper.toDTO(detail) : null;
    }

    @PostMapping
    public VirtualProductDetailsDTO addVirtualProductDetails(@RequestBody VirtualProductDetailsDTO dto) {
        VirtualProductDetails detail = VirtualProductDetailsDTOMapper.fromDTO(dto);
        VirtualProductDetails saved = virtualProductDetailsService.addVirtualProductDetails(detail);
        return VirtualProductDetailsDTOMapper.toDTO(saved);
    }

    @PutMapping("/{id}")
    public VirtualProductDetailsDTO updateVirtualProductDetails(@PathVariable Long id, @RequestBody VirtualProductDetailsDTO dto) {
        VirtualProductDetails detail = VirtualProductDetailsDTOMapper.fromDTO(dto);
        VirtualProductDetails updated = virtualProductDetailsService.updateVirtualProductDetails(id, detail);
        return updated != null ? VirtualProductDetailsDTOMapper.toDTO(updated) : null;
    }

    @DeleteMapping("/{id}")
    public void deleteVirtualProductDetails(@PathVariable Long id) {
        virtualProductDetailsService.deleteVirtualProductDetails(id);
    }
}

