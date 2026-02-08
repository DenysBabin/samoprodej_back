package samoprodej.samoprodej.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import samoprodej.samoprodej.config.SecurityUser;
import samoprodej.samoprodej.dto.auth.AuthResponse;
import samoprodej.samoprodej.dto.auth.LoginRequest;
import samoprodej.samoprodej.dto.auth.RegisterRequest;
import samoprodej.samoprodej.dto.user.UserResponse; // <--- Додано імпорт
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.AuthProvider;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.UserStatus;
import samoprodej.samoprodej.mapper.UserMapper; // <--- Додано імпорт
import samoprodej.samoprodej.repository.UserRepository;
import samoprodej.samoprodej.service.JwtService;
import samoprodej.samoprodej.service.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        var user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);
        try {
            user.setPreferredLang(Language.valueOf(request.getPreferredLang().toUpperCase()));
        } catch (Exception e) {
            user.setPreferredLang(Language.CS);
        }

        userRepository.save(user);

        return authenticateAndRespond(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return authenticateAndRespond(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refresh_token") String refreshToken) {
        try {
            String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
            User user = refreshTokenService.getUserFromToken(newRefreshToken);
            SecurityUser securityUser = new SecurityUser(user);

            String newAccessToken = jwtService.generateAccessToken(securityUser, user.getId(), user.getRole().name());

            ResponseCookie cookie = buildRefreshCookie(newRefreshToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(AuthResponse.builder()
                            .accessToken(newAccessToken)
                            .expiresInSec(900)
                            .build());

        } catch (Exception e) {
            ResponseCookie cookie = buildRefreshCookie("");
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.revokeToken(refreshToken);
        }
        ResponseCookie cookie = buildRefreshCookie("");
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    private ResponseEntity<AuthResponse> authenticateAndRespond(User user) {
        SecurityUser securityUser = new SecurityUser(user);
        String accessToken = jwtService.generateAccessToken(securityUser, user.getId(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        ResponseCookie cookie = buildRefreshCookie(refreshToken);

        UserResponse userResponse = userMapper.toDto(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(AuthResponse.builder()
                        .accessToken(accessToken)
                        .expiresInSec(900)
                        .user(userResponse)
                        .build());
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(token.isEmpty() ? 0 : 30 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }
}