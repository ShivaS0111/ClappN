package biz.craftline.server.feature.businessstore.domain.model;

import biz.craftline.server.feature.address.domain.model.Address;
import lombok.Data;

import java.util.Set;

@Data
public class Store {
    private Long id;

    private String storeName;

    private String description;

    private int status;

    //private Business business;

    private Address address;

    private Set<StoreOfferedService> services = Set.of();

}
