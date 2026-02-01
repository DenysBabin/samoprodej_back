package samoprodej.samoprodej.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import samoprodej.samoprodej.enums.AuthProvider;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // === Main Fields ===
    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "display_name")
    private String displayName;

    @Column(length = 32, unique = true)
    private String phone;

    @Column(length = 320, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_lang", length = 2) // cs, ru, en
    private Language preferredLang = Language.CS; // Default: Czech

    // === Auth & Security Fields ===
    private String login;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 16)
    private AuthProvider authProvider;

    // === Verification Fields ===
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "phone_verified_at")
    private LocalDateTime phoneVerifiedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}