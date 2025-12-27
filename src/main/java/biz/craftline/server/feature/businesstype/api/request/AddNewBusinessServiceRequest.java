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
public class AddNewBusinessServiceRequest {

    @NotNull(message = "Service name should not be null")
    private String name;

    private String desc;

    private Integer status;

    @NotNull(message = "businessType should not be null")
    private long businessTypeId;

    private List<Long> categoryIds;

    private float amount;

    private Long currency;
    private Long duration;//in minutes

}