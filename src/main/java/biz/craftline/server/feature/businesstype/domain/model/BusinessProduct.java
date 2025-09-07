package biz.craftline.server.feature.businesstype.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessProduct {
    private Long id;
    private String name;
    private String description;
    private int status;
    private Category category;
    private float amount;
    private Long currency;
}
