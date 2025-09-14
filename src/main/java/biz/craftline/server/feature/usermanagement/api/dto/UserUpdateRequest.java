package biz.craftline.server.feature.usermanagement.api.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class UserUpdateRequest {
    private String fullName;
    private String email;
    private boolean enabled;
    // Add other fields as needed (address, etc.)

}
