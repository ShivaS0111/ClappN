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
public class BusinessService {
    private Long id;
    private String serviceName;
    private String description;
    private Integer status;
    private BusinessType businessType;
    private Float amount;
    private Long currency;
    private List<Category> categories;
    private Long duration;//in minutes
}
