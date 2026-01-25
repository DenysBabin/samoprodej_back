package samoprodej.auth.DTO;

import jakarta.validation.constraints.*;

public record AuthResponse(String accessToken) {}