package samoprodej.auth.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import samoprodej.auth.DTO.AuthResponse;
import samoprodej.auth.DTO.LoginRequest;
import samoprodej.auth.DTO.RegisterRequest;
import samoprodej.auth.service.LoginService;
import samoprodej.auth.service.RegisterService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;

    public AuthController(RegisterService registerService, LoginService loginService) {
        this.registerService = registerService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(registerService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(loginService.login(req));
    }
}
