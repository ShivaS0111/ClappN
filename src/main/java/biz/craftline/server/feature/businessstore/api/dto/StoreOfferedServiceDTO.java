package biz.craftline.server.feature.businessstore.api.dto;

import biz.craftline.server.feature.businesstype.api.dto.BusinessServiceDTO;
import biz.craftline.server.feature.businesstype.domain.model.BusinessService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreOfferedServiceDTO {

    private Long id;

    private String aliasName;

    private Long storeId;

    private int status;

    private BusinessServiceDTO service;

    private StoreItemPriceDTO price;
}
