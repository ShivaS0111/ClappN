package biz.craftline.server.feature.businessstore.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddNewStoreOfferedServiceRequest {

    private String aliasName;

    private Long storeId;

    private int status;

    private long businessServiceId;

    private long storeServicePriceId;

}
