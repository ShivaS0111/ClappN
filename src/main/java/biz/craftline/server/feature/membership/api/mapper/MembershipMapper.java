package biz.craftline.server.feature.membership.api.mapper;

import biz.craftline.server.feature.membership.api.dto.MembershipRequest;
import biz.craftline.server.feature.membership.api.dto.MembershipResponse;
import biz.craftline.server.feature.membership.domain.model.Membership;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;

import java.util.HashSet;
import java.util.stream.Collectors;

public final class MembershipMapper {

    private MembershipMapper() {}

    public static Membership fromEntity(MembershipEntity entity) {
        Membership m = new Membership();
        m.setId(entity.getId());
        m.setUserId(entity.getUserId());
        m.setBusinessId(entity.getBusinessId());
        m.setStatus(entity.getStatus());
        if (entity.getRoles() != null) {
            m.setRoleIds(entity.getRoles().stream()
                    .map(RoleEntity::getId)
                    .collect(Collectors.toCollection(HashSet::new)));
        }
        if (entity.getStoreScopes() != null) {
            m.setStoreIds(new HashSet<>(entity.getStoreScopes()));
        }
        return m;
    }

    public static MembershipResponse toResponse(Membership m) {
        MembershipResponse r = new MembershipResponse();
        r.setId(m.getId());
        r.setUserId(m.getUserId());
        r.setBusinessId(m.getBusinessId());
        r.setStatus(m.getStatus());
        r.setRoleIds(m.getRoleIds());
        r.setStoreIds(m.getStoreIds());
        return r;
    }

    public static Membership toDomain(MembershipRequest req) {
        Membership m = new Membership();
        m.setUserId(req.getUserId());
        m.setBusinessId(req.getBusinessId());
        m.setStatus(req.getStatus());
        if (req.getRoleIds() != null) {
            m.setRoleIds(new HashSet<>(req.getRoleIds()));
        }
        if (req.getStoreIds() != null) {
            m.setStoreIds(new HashSet<>(req.getStoreIds()));
        }
        return m;
    }
}
