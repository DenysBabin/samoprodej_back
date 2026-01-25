package samoprodej.auth.domain;

public record AuthUser(
        String email,
        String passwordHash,
        String role,     // например "USER"
        boolean enabled
) {}
