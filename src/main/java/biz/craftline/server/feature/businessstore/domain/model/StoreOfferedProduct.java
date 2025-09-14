package biz.craftline.server.feature.businessstore.domain.model;

import biz.craftline.server.feature.businesstype.domain.model.BusinessProduct;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreOfferedProduct {

    private Long id;

    private String aliasName;
    private String description;

    private Long storeId;

    private int status;

    private BusinessProduct product;

    private StoreItemPrice price;
}
