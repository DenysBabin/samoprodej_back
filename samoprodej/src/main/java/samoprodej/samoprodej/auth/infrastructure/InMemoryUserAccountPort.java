package samoprodej.samoprodej.auth.infrastructure;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import samoprodej.samoprodej.auth.domain.AuthUser;
import samoprodej.samoprodej.auth.domain.UserAccountPort;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class InMemoryUserAccountPort implements UserAccountPort {

    private final ConcurrentHashMap<String, AuthUser> store = new ConcurrentHashMap<>();

    @Override
    public boolean existsByEmail(String email) {
        return store.containsKey(email);
    }

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return Optional.ofNullable(store.get(email));
    }

    @Override
    public AuthUser create(String email, String passwordHash) {
        AuthUser u = new AuthUser(email, passwordHash, "USER", true);
        store.put(email, u);
        return u;
    }
}
