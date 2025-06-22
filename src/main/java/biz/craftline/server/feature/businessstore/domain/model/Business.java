package biz.craftline.server.feature.businessstore.domain.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Business {

    private Long id;

    private String businessName;

    private String description;

    private int status;

    private Set<Store> stores = new HashSet<>();

}
