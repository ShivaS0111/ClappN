package biz.craftline.server.feature.businessstore.api.dto;

import lombok.Data;

@Data
public class StoreServicePriceDTO {

    private Long id;

    private Long serviceId;

    private Double price;

    private String currencyId;

    private int status;
}
