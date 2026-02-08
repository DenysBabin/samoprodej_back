package samoprodej.samoprodej.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import samoprodej.samoprodej.enums.Language;

public record UpdateUserRequest(
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 32)
        String phone,

        @Size(max = 64)
        String firstName,

        @Size(max = 64)
        String lastName,

        Language preferredLang,

        String avatarUrl
) {
}
