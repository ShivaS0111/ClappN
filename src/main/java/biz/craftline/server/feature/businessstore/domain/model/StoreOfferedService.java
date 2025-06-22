package biz.craftline.server.feature.businessstore.domain.model;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import lombok.Data;


@Data
public class StoreOfferedService {

    private Long id;

    private String aliasName;

    private Long storeId;

    private int status;

    private BusinessService service;

    private StoreServicePrice price;
}
