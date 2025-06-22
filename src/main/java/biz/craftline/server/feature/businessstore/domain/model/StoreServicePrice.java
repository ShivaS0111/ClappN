package biz.craftline.server.feature.businessstore.domain.model;

import lombok.Data;

@Data
public class StoreServicePrice {

    private Long id;

    private Long serviceId;

    private Double price;

    private String currencyId;

    private int status;
}
