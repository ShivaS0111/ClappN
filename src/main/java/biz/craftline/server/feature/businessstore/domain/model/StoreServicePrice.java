package biz.craftline.server.feature.businessstore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreServicePrice {

    private Long id;

    private Long serviceId;

    private Double price;

    private String currencyId;

    private int status;
}
