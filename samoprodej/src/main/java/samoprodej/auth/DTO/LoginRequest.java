package samoprodej.auth.DTO;
import jakarta.validation.constraints.*;

public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}