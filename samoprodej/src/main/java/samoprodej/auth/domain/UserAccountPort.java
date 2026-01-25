package samoprodej.auth.domain;

import java.util.Optional;

public interface UserAccountPort {
    boolean existsByEmail(String email);
    Optional<AuthUser> findByEmail(String email);


    AuthUser create(String email, String passwordHash);
}
