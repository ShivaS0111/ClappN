package biz.craftline.server.feature.businessstore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Business {

    private Long id;

    private String businessName;

    private String description;

    private int status;

    private Set<Store> stores = new HashSet<>();

}
