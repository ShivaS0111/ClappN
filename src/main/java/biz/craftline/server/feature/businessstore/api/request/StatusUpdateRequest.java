package biz.craftline.server.feature.businessstore.api.request;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Business ID is required") Long id,
        @NotNull(message = "Business status is required")Integer status
) {
}
