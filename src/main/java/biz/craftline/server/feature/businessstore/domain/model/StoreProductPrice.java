package biz.craftline.server.feature.businessstore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreProductPrice {

    private Long id;

    private Long productId;

    private Integer productType;

    private Double price;

    private String currencyId;

    private String countryId;

    private int status;
}
