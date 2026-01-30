package samoprodej.samoprodej.auth.service;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import samoprodej.samoprodej.auth.DTO.AuthResponse;
import samoprodej.samoprodej.auth.DTO.RegisterRequest;
import samoprodej.samoprodej.auth.domain.AuthUser;
import samoprodej.samoprodej.auth.domain.UserAccountPort;
import samoprodej.samoprodej.auth.security.JwtService;

@Service
public class RegisterService {

    private final UserAccountPort users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public RegisterService(UserAccountPort users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = normalize(req.email());

        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String hash = encoder.encode(req.password());

        AuthUser created = users.create(email, hash);

        if (!created.enabled()) {
            throw new IllegalStateException("User disabled");
        }

        String token = jwt.issue(created.email(), created.role());
        return new AuthResponse(token);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}

