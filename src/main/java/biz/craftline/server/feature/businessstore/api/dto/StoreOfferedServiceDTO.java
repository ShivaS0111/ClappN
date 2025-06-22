package biz.craftline.server.feature.businessstore.api.dto;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import lombok.Data;

@Data
public class StoreOfferedServiceDTO {

    private Long id;

    private String aliasName;

    private Long storeId;

    private int status;

    private BusinessService service;

    private StoreServicePriceDTO price;
}
