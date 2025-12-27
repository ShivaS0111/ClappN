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

    private Long businessTypeId;
    private Long brandId;

    //@NotNull(message = "categories should not be null")
    private List<Long> categories;

    private float amount;

    private Long currency;

}