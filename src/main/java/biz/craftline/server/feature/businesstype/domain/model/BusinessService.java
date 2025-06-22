package biz.craftline.server.feature.businesstype.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessService {
    private Long id;
    private String serviceName;
    private String description;
    private int status;
    private BusinessType businessType;
    private float amount;
    private Long currency;
}
