package biz.craftline.server.feature.businessstore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreDTO {
    private Long id;

    private String storeName;

    private String description;

    private int status;

    private long businessType;

    private BusinessDTO business;

    private long address;

    private Set<StoreOfferedServiceDTO> services = Set.of();

}
