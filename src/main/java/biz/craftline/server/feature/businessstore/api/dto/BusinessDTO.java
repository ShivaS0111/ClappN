package biz.craftline.server.feature.businessstore.api.dto;

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
public class BusinessDTO {

    private Long id;

    private String businessName;

    private String description;

    private int status;

    private long createdBy;

    private Set<StoreDTO> stores = new HashSet<>();

}
