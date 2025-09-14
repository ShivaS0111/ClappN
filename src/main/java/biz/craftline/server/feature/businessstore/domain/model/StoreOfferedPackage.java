package biz.craftline.server.feature.businessstore.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class StoreOfferedPackage {
    // Getters and setters
    private Long id;
    private Long storeId;
    private String name;
    private String description;
    private Set<StoreOfferedProduct> products;
    private Set<StoreOfferedService> services;
    private Double price;
    private Boolean available;

}
