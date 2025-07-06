package biz.craftline.server.feature.businessstore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreItemPriceDTO {

    private Long id;

    private Long productLotId;

    private Long serviceId;

    private Double price;

    private CurrencyDTO currency;
    private Long countryId;

    private int status;
}
