package biz.craftline.server.feature.businessstore.api.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class BusinessDTO {

    private Long id;

    private String businessName;

    private String description;

    private int status;

    private Set<StoreDTO> stores = new HashSet<>();

}
