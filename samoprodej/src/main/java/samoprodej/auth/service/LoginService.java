package samoprodej.auth.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import samoprodej.auth.DTO.AuthResponse;
import samoprodej.auth.DTO.LoginRequest;
import samoprodej.auth.domain.AuthUser;
import samoprodej.auth.domain.UserAccountPort;
import samoprodej.auth.security.JwtService;

@Service
public class LoginService {

    private final UserAccountPort users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public LoginService(UserAccountPort users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse login(LoginRequest req) {
        String email = normalize(req.email());

        AuthUser u = users.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!u.enabled()) {
            throw new BadCredentialsException("User disabled");
        }

        if (!encoder.matches(req.password(), u.passwordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwt.issue(u.email(), u.role());
        return new AuthResponse(token);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
