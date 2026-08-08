import biz.craftline.server.config.security.UserScopeContext;
import biz.craftline.server.config.security.UserScopeContextHolder;
import biz.craftline.server.feature.usermanagement.api.dto.MeContextResponse;
import biz.craftline.server.util.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight endpoint exposing the current request's user/store/business scope.
 * Requires authentication; scope is resolved from DB by UserScopeFilter.
 */
@RestController
@RequestMapping("/api/me")
public class MeContextController {

    @GetMapping("/context")
    @Operation(summary = "Get current user scope context for this request")
    public ResponseEntity<APIResponse<MeContextResponse>> getContext() {
        UserScopeContext scope = UserScopeContextHolder.require();
        MeContextResponse body = MeContextResponse.builder()
                .userId(scope.getUserId())
                .email(scope.getEmail())
                .roles(scope.getRoles())
                .permissions(scope.getPermissions())
                .accessibleStoreIds(scope.getAccessibleStoreIds())
                .accessibleBusinessIds(scope.getAccessibleBusinessIds())
                .activeStoreId(scope.getActiveStoreId())
                .activeBusinessId(scope.getActiveBusinessId())
                .unrestricted(scope.isUnrestricted())
                .build();
        return APIResponse.success(body, "Current user context");
    }
}
