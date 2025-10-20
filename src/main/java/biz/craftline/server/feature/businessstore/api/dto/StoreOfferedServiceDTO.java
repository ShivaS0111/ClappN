package biz.craftline.server.feature.businessstore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreOfferedServiceDTO {

    private Long id;

    private String aliasName;

    private String description;

    private Long storeId;

    private int status;

    private Long businessServiceId;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private StoreItemPriceDTO price;
}
