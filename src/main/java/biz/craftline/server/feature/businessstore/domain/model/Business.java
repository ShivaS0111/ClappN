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
    private String contact;
    private String email;
    private String website;
    private String address;
    private Double latitude;
    private Double longitude;
    private Long createdBy;

    private Integer status;

    private Set<Store> stores = new HashSet<>();

}
