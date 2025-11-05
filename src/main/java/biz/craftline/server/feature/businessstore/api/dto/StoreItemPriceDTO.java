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

    private Long itemType;

    private Long itemId;

    private Double price;

    private Long currency;
    private Long countryId;

    private int status;
}
