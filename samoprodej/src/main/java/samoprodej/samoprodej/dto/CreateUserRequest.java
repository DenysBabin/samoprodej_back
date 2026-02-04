package samoprodej.samoprodej.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import samoprodej.samoprodej.enums.AuthProvider;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.Role;

public record CreateUserRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 32)
        String phone,

        @NotBlank
        @Size(min = 6, max = 100)
        String password,

        @Size(max = 64)
        String firstName,

        @Size(max = 64)
        String lastName,

        @NotNull
        Role role,

        @NotNull
        AuthProvider authProvider,

        Language preferredLang
) {
}
