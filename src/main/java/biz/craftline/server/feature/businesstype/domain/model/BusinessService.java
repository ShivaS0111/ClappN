package biz.craftline.server.feature.businesstype.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BusinessService {
    private Long id;
    private String serviceName;
    private String description;
    private int status;
    private Long businessType;
    private float amount;
    private Long currency;
}
