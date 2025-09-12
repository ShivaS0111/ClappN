package biz.craftline.server.feature.businessstore.domain.model;

import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class StoreOfferedService {

    private Long id;

    private String aliasName;

    private Long storeId;

    private int status;

    private Long serviceId;

    private BusinessService service;

    private StoreItemPrice price;
}
