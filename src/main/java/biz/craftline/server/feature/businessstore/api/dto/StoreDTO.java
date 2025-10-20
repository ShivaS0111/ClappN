package biz.craftline.server.feature.businessstore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
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

    private Long businessType;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private BusinessDTO business;

    private Long addressId;

    @Builder.Default
    private Set<StoreOfferedServiceDTO> services = new HashSet<>();

    @Builder.Default
    private Set<StoreOfferedProductDTO> products = new HashSet<>();
}
