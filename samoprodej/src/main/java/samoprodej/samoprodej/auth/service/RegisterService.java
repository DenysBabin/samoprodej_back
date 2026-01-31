package samoprodej.samoprodej.auth.service;


import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import samoprodej.samoprodej.Entity.User;
import samoprodej.samoprodej.Enums.AuthProvider;
import samoprodej.samoprodej.Repository.UserRepository;
import samoprodej.samoprodej.auth.DTO.RegisterRequest;

@Service
public class RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        String hash = passwordEncoder.encode(req.password());

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hash);      // <-- подставьте реальное имя поля/сеттера
        user.setAuthProvider(AuthProvider.LOCAL); // если поле есть

        userRepository.save(user);
    }
}
