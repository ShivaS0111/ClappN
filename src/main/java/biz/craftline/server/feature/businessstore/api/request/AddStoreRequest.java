package biz.craftline.server.feature.businessstore.api.request;

import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddStoreRequest {
    private Long id;
    private String name;
    private String description;
    private int status;

    public static Store getStore(StoreDTO dto) {
        Store store = new Store();
        //store.setStoreName(dto.getStoreName());
        //store.setDescription(dto.getDescription());
        //store.setStatus(dto.getStatus());
        return store;
    }

    public static StoreDTO getStoreDTO(Store store) {
        StoreDTO storedto =  new StoreDTO();
        //storedto.setStoreName(store.getStoreName());
        //storedto.setDescription(store.getDescription());
        //storedto.setStatus(store.getStatus());
        return storedto;
    }
}
