package samoprodej.samoprodej.dto.auth;

import lombok.Builder;
import lombok.Data;
import samoprodej.samoprodej.dto.user.UserResponse;

@Data
@Builder
public class AuthResponse {
    private UserResponse user;
    private String accessToken;
    private long expiresInSec;
}