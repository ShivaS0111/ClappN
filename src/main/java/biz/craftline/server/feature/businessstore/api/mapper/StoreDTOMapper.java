package biz.craftline.server.feature.businessstore.api.mapper;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.request.AddNewStoreRequest;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreDTOMapper {

    @Autowired
    BusinessDTOMapper mapper;

    public Store toDomain(AddNewStoreRequest request){

        return Store.builder()
                .storeName(request.getStoreName())
                .description(request.getDescription())
                .businessType(request.getBusinessType())
                .address(request.getAddress())
                .build();
    }

    public Store toDomain(StoreDTO dto){

        Business business=null;
        if(dto.getBusiness()!=null){
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

    public StoreDTO toDTO(Store store){

        BusinessDTO businessDTO=null;
        if(store.getBusiness()!=null){
            businessDTO = mapper.toDTO(store.getBusiness());
        }

        return StoreDTO.builder()
                .id(store.getId())
                .storeName(store.getStoreName())
                .description(store.getDescription())
                .business(businessDTO)
                .address(store.getAddress())
                .build();
    }

}
