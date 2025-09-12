package biz.craftline.server.feature.businessstore.domain.model;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class Business {

    private Long id;

    private String businessName;

    private String description;

    private int status;

    private Set<Store> stores = new HashSet<>();

}
