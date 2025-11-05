package biz.craftline.server.feature.businesstype.domain.model;

import biz.craftline.server.feature.businesstype.api.dto.BusinessTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessProduct {
    private Long id;
    private String name;
    private String description;
    private int status;
    private BusinessType businessType;
    private List<Category> categories;
    private float amount;
    private Long currency;
}
