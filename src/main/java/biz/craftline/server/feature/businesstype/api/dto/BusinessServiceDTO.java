package biz.craftline.server.feature.businesstype.api.dto;

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

    private Integer status;

    private BusinessTypeDTO businessType;

    private float amount;

    private Long currency;

}
