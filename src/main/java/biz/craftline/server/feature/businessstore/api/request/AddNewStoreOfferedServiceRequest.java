package biz.craftline.server.feature.businessstore.api.request;

import biz.craftline.server.feature.businessstore.api.dto.StoreServicePriceDTO;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddNewStoreOfferedServiceRequest {

    private Long id;

    private String aliasName;

    private Long storeId;

    private int status;

    private long businessServiceId;

    private long storeServicePriceId;

}
