package samoprodej.samoprodej.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import samoprodej.samoprodej.entity.RefreshToken;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.repository.RefreshTokenRepository;
import samoprodej.samoprodej.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String rawToken = generateRandomToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public String rotateRefreshToken(String oldRawToken) {
        String oldHash = hashToken(oldRawToken);

        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (oldToken.isRevoked()) {

            revokeAllUserTokens(oldToken.getUser());
            throw new RuntimeException("Refresh token reusing detected. All sessions revoked.");
        }

        if (oldToken.isExpired()) {
            throw new RuntimeException("Refresh token expired");
        }

        oldToken.setRevokedAt(Instant.now());

        String newRawToken = generateRandomToken();
        String newTokenHash = hashToken(newRawToken);

        RefreshToken newToken = RefreshToken.builder()
                .user(oldToken.getUser())
                .tokenHash(newTokenHash)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        RefreshToken savedNewToken = refreshTokenRepository.save(newToken);
        oldToken.setReplacedByTokenId(savedNewToken.getId());
        refreshTokenRepository.save(oldToken);

        return newRawToken;
    }

    @Transactional
    public User getUserFromToken(String rawToken) {
        String hash = hashToken(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        if (!token.isActive()) {
            throw new RuntimeException("Token inactive");
        }
        return token.getUser();
    }

    @Transactional
    public void revokeToken(String rawToken) {
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        var validTokens = refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user);
        if (validTokens.isEmpty()) return;
        validTokens.forEach(t -> t.setRevokedAt(Instant.now()));
        refreshTokenRepository.saveAll(validTokens);
    }

    private String generateRandomToken() {
        SecureRandom sr = new SecureRandom();
        byte[] bytes = new byte[64];
        sr.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}