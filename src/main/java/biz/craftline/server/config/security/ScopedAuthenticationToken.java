package biz.craftline.server.config.security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Extended authentication token that carries RBAC scope data (roles, storeIds, businessIds)
 * extracted from JWT claims. This allows downstream services to access the user's scope
 * without re-querying the database.
 */
@Getter
public class ScopedAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final List<String> roles;
    private final List<Long> storeIds;
    private final List<Long> businessIds;

    public ScopedAuthenticationToken(Object principal, Object credentials,
                                      Collection<? extends GrantedAuthority> authorities,
                                      List<String> roles, List<Long> storeIds, List<Long> businessIds) {
        super(principal, credentials, authorities);
        this.roles = roles != null ? roles : List.of();
        this.storeIds = storeIds != null ? storeIds : List.of();
        this.businessIds = businessIds != null ? businessIds : List.of();
    }
}

