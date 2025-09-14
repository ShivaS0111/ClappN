package biz.craftline.server.feature.businessstore.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class StoreOfferedPackageDTO {
    // Getters and setters
    private Long id;
    private Long storeId;
    private String name;
    private String description;
    private Set<Long> productIds;
    private Set<Long> serviceIds;
    private Double price;
    private Boolean available;

}
