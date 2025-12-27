package biz.craftline.server.feature.businesstype.domain.model;

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
    private Float amount;
    private Long currency;
    private Brand brand;
}
