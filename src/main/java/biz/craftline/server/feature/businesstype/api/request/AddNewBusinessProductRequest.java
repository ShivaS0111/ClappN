package biz.craftline.server.feature.businesstype.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddNewBusinessProductRequest {

    @NotNull(message = "Product name should not be null")
    private String name;

    private String desc;

    private int status;

    /** Tenant that owns this catalog item. Required for non–SYSTEM_ADMIN creators. */
    private Long businessId;

    private Long businessTypeId;
    private Long brandId;

    //@NotNull(message = "categories should not be null")
    private List<Long> categories;

    /** Optional business default price; omit if unset. */
    private Float amount;

    private Long currency;

    private String thumbnailUrl;

    private String galleryUrls;

}