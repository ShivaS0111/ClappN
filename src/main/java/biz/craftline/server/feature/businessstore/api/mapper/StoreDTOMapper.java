package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between StoreDTO, AddNewStoreRequest, and Store domain model.
 */
@Component
public class StoreDTOMapper {
    private final BusinessDTOMapper mapper;

    public StoreDTOMapper(BusinessDTOMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Converts an AddNewStoreRequest to a Store domain model.
     * @param request the AddNewStoreRequest
     * @return the Store domain model
     */
    public Store toDomain(AddNewStoreRequest request) {
        return Store.builder()
                .storeName(request.getStoreName())
                .description(request.getDescription())
                .businessType(request.getBusinessType())
                .address(request.getAddress())
                .build();
    }

    /**
     * Converts a StoreDTO to a Store domain model.
     * @param dto the StoreDTO
     * @return the Store domain model
     */
    public Store toDomain(StoreDTO dto) {
        Business business = null;
        if (dto.getBusiness() != null) {
            business = mapper.toDomain(dto.getBusiness());
        }
        return Store.builder()
                .id(dto.getId())
                .storeName(dto.getStoreName())
                .description(dto.getDescription())
                .business(business)
                .businessType(dto.getBusinessType())
                .address(dto.getAddress())
                .build();
    }

    /**
     * Converts a Store domain model to a StoreDTO.
     * @param store the Store domain model
     * @return the StoreDTO
     */
    public StoreDTO toDTO(Store store) {
        BusinessDTO businessDTO = null;
        if (store.getBusiness() != null) {
            businessDTO = mapper.toDTO(store.getBusiness());
        }
        return StoreDTO.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .description(store.getDescription())
                .business(businessDTO)
                .businessType(store.getBusinessType())
                .address(store.getAddress())
                .build();
    }
}
