package samoprodej.samoprodej.auth.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.Entity.User;
import samoprodej.samoprodej.Repository.UserRepository;
import samoprodej.samoprodej.auth.DTO.AuthResponse;
import samoprodej.samoprodej.auth.DTO.LoginRequest;
import samoprodej.samoprodej.auth.DTO.RegisterRequest;
import samoprodej.samoprodej.auth.security.JwtService;
import samoprodej.samoprodej.auth.service.RegisterService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterService registerService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(RegisterService registerService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.registerService = registerService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        registerService.register(req);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId().toString());
        return ResponseEntity.ok(new AuthResponse(token));
    }




}
