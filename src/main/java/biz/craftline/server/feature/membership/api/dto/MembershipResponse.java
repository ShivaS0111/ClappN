package biz.craftline.server.feature.membership.api.dto;

import lombok.Data;

import java.util.Set;

@Data
public class MembershipResponse {
    private Long id;
    private Long userId;
    private Long businessId;
    private String status;
    private Set<Long> roleIds;
    private Set<Long> storeIds;
}
