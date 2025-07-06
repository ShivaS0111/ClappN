package biz.craftline.server.feature.businessstore.domain.model;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreOfferedService {

    private Long id;

    private String aliasName;

    private Long storeId;

    private int status;

    private BusinessService service;

    private StoreProductPrice price;
}
