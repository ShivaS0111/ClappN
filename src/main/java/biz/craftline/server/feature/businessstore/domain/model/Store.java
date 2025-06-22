package biz.craftline.server.feature.businessstore.domain.model;

import biz.craftline.server.feature.address.domain.model.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Store {
    private Long id;

    private String storeName;

    private String description;

    private int status;

    private long businessType;

    private Business business;

    private long address;

    private Set<StoreOfferedService> services = Set.of();

}
