package samoprodej.samoprodej.dto.auth;

import lombok.Data;
import samoprodej.samoprodej.enums.Role;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Role role;
    private String preferredLang;
}
