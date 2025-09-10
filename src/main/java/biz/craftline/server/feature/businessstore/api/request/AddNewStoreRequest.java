package biz.craftline.server.feature.businessstore.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddNewStoreRequest {

    @NotNull(message = "Store name is required")
    @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
    private String storeName;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    private int status;

    private long businessType;

    @NotNull(message = "Business ID is required")
    private Long businessId;

    private long address;

}
