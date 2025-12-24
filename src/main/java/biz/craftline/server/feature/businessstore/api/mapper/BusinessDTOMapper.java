package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewBusinessRequest;
import biz.craftline.server.feature.businessstore.api.request.UpdateBusinessRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Component
public class BusinessDTOMapper {

    @Lazy
    @Autowired
    StoreDTOMapper storeDTOMapper;

    public Business toDomain(AddNewBusinessRequest request) {
        return Business.builder()
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .contact(request.getContact())
                .status(request.getStatus())
                .email(request.getEmail())
                .website(request.getWebsite())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    public Business toDomain(UpdateBusinessRequest request) {
        return Business.builder()
                .id(request.getId())
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .status(request.getStatus())
                .contact(request.getContact())
                .email(request.getEmail())
                .website(request.getWebsite())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    public Business toUpdated(Business old, Business businessUpdator) {

        if(businessUpdator.getBusinessName()!=null ) old.setBusinessName(businessUpdator.getBusinessName());
        if(businessUpdator.getDescription()!=null ) old.setDescription(businessUpdator.getDescription());
        if(businessUpdator.getStatus()!=null ) old.setStatus(businessUpdator.getStatus());
        if(businessUpdator.getContact()!=null ) old.setContact(businessUpdator.getContact());
        if(businessUpdator.getEmail()!=null ) old.setEmail(businessUpdator.getEmail());
        if(businessUpdator.getWebsite()!=null ) old.setWebsite(businessUpdator.getWebsite());
        if(businessUpdator.getAddress()!=null ) old.setAddress(businessUpdator.getAddress());
        if(businessUpdator.getLatitude()!=null ) old.setLatitude(businessUpdator.getLatitude());
        if(businessUpdator.getLongitude()!=null ) old.setLongitude(businessUpdator.getLongitude());
        if(businessUpdator.getCreatedBy()!=null ) old.setCreatedBy(businessUpdator.getCreatedBy());
        return old;
    }

    public Business toDomain(BusinessDTO dto) {
        return Business.builder()
                .id(dto.getId())
                .businessName(dto.getBusinessName())
                .description(dto.getDescription())
                .contact(dto.getContact())
                .email(dto.getEmail())
                .website(dto.getWebsite())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .createdBy(dto.getCreatedBy())
                .status(dto.getStatus())
                .build();
    }

    public BusinessDTO toDTO(Business business) {
        Set<StoreDTO> storeDTOS = null;

        if (business.getStores() != null) {
            storeDTOS = business.getStores()
                    .stream()
                    .map(item -> storeDTOMapper.toDTO(item))
                    .collect(Collectors.toSet());
        }

        return BusinessDTO.builder()
                .id(business.getId())
                .businessName(business.getBusinessName())
                .description(business.getDescription())
                .contact(business.getContact())
                .email(business.getEmail())
                .website(business.getWebsite())
                .address(business.getAddress())
                .latitude(business.getLatitude())
                .longitude(business.getLongitude())
                .createdBy(business.getCreatedBy())
                .status(business.getStatus())
                .stores(storeDTOS)
                .build();
    }
}
