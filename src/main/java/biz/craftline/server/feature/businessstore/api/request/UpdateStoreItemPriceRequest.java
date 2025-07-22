package biz.craftline.server.feature.businessstore.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateStoreItemPriceRequest {

    private Long id;

    private Long productLotId;

    private Long serviceId;

    private Double price;

    private Long currencyId;

    private Long countryId;

    private int status;
}
