package biz.craftline.server.feature.businesstype.api.dto;


import biz.craftline.server.feature.businesstype.domain.model.Brand;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessProductDTO {
    private Long id;

    private String name;

    private String desc;

    private int status;

    private BusinessTypeDTO businessType;

    private List<Category> categories;

    private float amount;

    private Long currency;

    private Brand brand;

}
