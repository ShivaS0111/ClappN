package biz.craftline.server.feature.businessstore.api.dto;

import biz.craftline.server.feature.address.domain.model.Address;
import lombok.Data;

import java.util.Set;

@Data
public class StoreDTO {
    private Long id;

    private String storeName;

    private String description;

    private int status;

    private BusinessDTO business;

    private Address address;

    private Set<StoreOfferedServiceDTO> services = Set.of();

}
