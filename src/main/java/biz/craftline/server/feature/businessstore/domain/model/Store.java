package biz.craftline.server.feature.businessstore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class Store {
    private Long id;

    private String storeName;

    private String description;

    private int status;

    private Long businessType;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Business business;

    private Long addressId;

    @Builder.Default
    private Set<StoreOfferedService> services = new HashSet<>();

    @Builder.Default
    private Set<StoreOfferedProduct> products = new HashSet<>();
}
