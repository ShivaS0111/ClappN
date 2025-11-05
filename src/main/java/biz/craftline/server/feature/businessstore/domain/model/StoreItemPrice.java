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

    private Long itemId;

    private Long itemType;

    private Double price;

    private Long currency;

    private Long countryId;

    private int status;
}
