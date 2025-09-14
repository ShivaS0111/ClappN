package biz.craftline.server.feature.ordermanagement.api.mapper;

import biz.craftline.server.feature.ordermanagement.api.dto.VirtualProductDetailsDTO;
import biz.craftline.server.feature.ordermanagement.domain.model.VirtualProductDetails;

public class VirtualProductDetailsDTOMapper {
    public static VirtualProductDetailsDTO toDTO(VirtualProductDetails model) {
        if (model == null) return null;
        VirtualProductDetailsDTO dto = new VirtualProductDetailsDTO();
        dto.setId(model.getId());
        dto.setLicenseKey(model.getLicenseKey());
        dto.setDownloadUrl(model.getDownloadUrl());
        dto.setActivationStatus(model.getActivationStatus());
        dto.setActivationDate(model.getActivationDate());
        return dto;
    }

    public static VirtualProductDetails fromDTO(VirtualProductDetailsDTO dto) {
        if (dto == null) return null;
        VirtualProductDetails model = new VirtualProductDetails();
        model.setId(dto.getId());
        model.setLicenseKey(dto.getLicenseKey());
        model.setDownloadUrl(dto.getDownloadUrl());
        model.setActivationStatus(dto.getActivationStatus());
        model.setActivationDate(dto.getActivationDate());
        return model;
    }
}
