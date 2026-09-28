package biz.craftline.server.feature.membership.api.dto;

import lombok.Data;

import java.util.Set;

@Data
public class MembershipRequest {
    private Long userId;
    private Long businessId;
    private String status;
    /** Role IDs to assign (replaces existing set when provided). */
    private Set<Long> roleIds;
    /** Store scope IDs (replaces existing set when provided). */
    private Set<Long> storeIds;
}
