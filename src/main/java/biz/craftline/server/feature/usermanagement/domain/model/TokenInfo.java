package biz.craftline.server.feature.usermanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenInfo {
    private String token;
    private String refreshToken;
    private Long tokenExpiry;
}
