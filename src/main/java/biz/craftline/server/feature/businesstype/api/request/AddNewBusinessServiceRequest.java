package biz.craftline.server.feature.businesstype.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddNewBusinessServiceRequest {

    @NotNull(message = "Service name should not be null")
    private String name;

    private String desc;

    private int status;

    @NotNull(message = "businessType should not be null")
    private long businessType;

    private float amount;

    private Long currency;

}