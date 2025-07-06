package biz.craftline.server.feature.businessstore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreProductPriceDTO {

    private Long id;

    private Long serviceId;

    private Double price;

    private String currencyId;
    private String countryId;

    private int status;
}
