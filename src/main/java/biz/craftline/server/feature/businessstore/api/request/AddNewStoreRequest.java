package biz.craftline.server.feature.businessstore.api.request;

import biz.craftline.server.feature.businessstore.api.dto.BusinessDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.dto.StoreOfferedServiceDTO;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddNewStoreRequest {

    private String storeName;

    private String description;

    private int status;

    private long businessType;

    private Long businessId;

    private long address;

}
