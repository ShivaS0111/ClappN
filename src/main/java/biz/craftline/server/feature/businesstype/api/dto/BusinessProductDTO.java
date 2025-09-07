package biz.craftline.server.feature.businesstype.api.dto;


import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessProductDTO {
    private Long id;

    private String name;

    private String desc;

    private int status;

    private Category category;

    private float amount;

    private Long currency;

}
