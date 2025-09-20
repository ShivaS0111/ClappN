package biz.craftline.server.feature.businessstore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreItemPrice {

    private Long id;

    private Long serviceId;

    private Long productLotId;

    private String itemName;

    private Double price;

    private Currency currency;

    private Long countryId;

    private int status;
}
