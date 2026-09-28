package biz.craftline.server.feature.membership.domain.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Membership {
    private Long id;
    private Long userId;
    private Long businessId;
    private String status;
    private Set<Long> roleIds = new HashSet<>();
    private Set<Long> storeIds = new HashSet<>();
}
