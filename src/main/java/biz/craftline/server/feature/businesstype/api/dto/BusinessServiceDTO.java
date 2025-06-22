package biz.craftline.server.feature.businesstype.api.dto;


import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessServiceDTO {
    private Long id;

    private String name;

    private String desc;

    private int status;

    private BusinessType businessType;

    private float amount;

    private Long currency;

}
