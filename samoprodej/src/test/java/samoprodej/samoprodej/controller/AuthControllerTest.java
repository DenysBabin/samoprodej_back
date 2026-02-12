package samoprodej.samoprodej.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import samoprodej.samoprodej.dto.user.UserResponse;
import samoprodej.samoprodej.entity.User;
import samoprodej.samoprodej.enums.AuthProvider;
import samoprodej.samoprodej.enums.Language;
import samoprodej.samoprodej.enums.Role;
import samoprodej.samoprodej.enums.UserStatus;
import samoprodej.samoprodej.mapper.UserMapper;
import samoprodej.samoprodej.repository.UserRepository;
import samoprodej.samoprodej.service.JwtService;
import samoprodej.samoprodej.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    AuthenticationManager authenticationManager;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    RefreshTokenService refreshTokenService;

    @MockitoBean
    UserMapper userMapper;

    static final String REGISTER_URL = "/api/auth/register";
    static final String LOGIN_URL = "/api/auth/login";

    User savedUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("encoded");
        user.setFirstName("Jan");
        user.setLastName("Novák");
        user.setRole(Role.TENANT);
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setPreferredLang(Language.CS);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    UserResponse userResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus(),
                user.getAuthProvider(),
                user.getPreferredLang(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("successful registration returns 200, accessToken and user")
        void register_success_returns200AndTokens() throws Exception {
            String email = "new@example.com";
            String password = "secret123";
            when(userRepository.existsByEmail(email)).thenReturn(false);
            when(passwordEncoder.encode(password)).thenReturn("encoded");

            User saved = savedUser();
            saved.setEmail(email);
            saved.setFirstName("Petr");
            saved.setLastName("Svoboda");
            saved.setRole(Role.OWNER);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(saved.getId());
                u.setCreatedAt(LocalDateTime.now());
                u.setUpdatedAt(LocalDateTime.now());
                return u;
            });

            when(jwtService.generateAccessToken(any(), eq(saved.getId()), any())).thenReturn("jwt.access.token");
            when(refreshTokenService.createRefreshToken(saved.getId())).thenReturn("refresh.token");
            when(userMapper.toDto(any(User.class))).thenReturn(userResponse(saved));

            String body = """
                    {
                      "email": "new@example.com",
                      "password": "secret123",
                      "firstName": "Petr",
                      "lastName": "Svoboda",
                      "role": "OWNER",
                      "preferredLang": "cs"
                    }
                    """;

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("jwt.access.token"))
                    .andExpect(jsonPath("$.expiresInSec").value(900))
                    .andExpect(jsonPath("$.user.email").value(email))
                    .andExpect(jsonPath("$.user.firstName").value("Petr"))
                    .andExpect(jsonPath("$.user.lastName").value("Svoboda"))
                    .andExpect(header().exists("Set-Cookie"));

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Registration with an existing email returns a 400")
        void register_duplicateEmail_returns400() throws Exception {
            String email = "existing@example.com";
            when(userRepository.existsByEmail(email)).thenReturn(true);

            String body = """
                    {
                      "email": "existing@example.com",
                      "password": "secret123",
                      "firstName": "Jan",
                      "lastName": "Novák",
                      "role": "TENANT"
                    }
                    """;

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("If preferredLang is unknown, CS is set by default")
        void register_unknownPreferredLang_usesDefault() throws Exception {
            when(userRepository.existsByEmail("u@x.com")).thenReturn(false);
            when(passwordEncoder.encode("pass")).thenReturn("enc");

            User saved = savedUser();
            saved.setEmail("u@x.com");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(saved.getId());
                u.setCreatedAt(LocalDateTime.now());
                u.setUpdatedAt(LocalDateTime.now());
                return u;
            });
            when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("token");
            when(refreshTokenService.createRefreshToken(any())).thenReturn("refresh");
            when(userMapper.toDto(any(User.class))).thenReturn(userResponse(saved));

            String body = """
                    {
                      "email": "u@x.com",
                      "password": "pass",
                      "firstName": "A",
                      "lastName": "B",
                      "role": "TENANT",
                      "preferredLang": "invalid"
                    }
                    """;

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("A successful login returns 200, accessToken, and user")
        void login_success_returns200AndTokens() throws Exception {
            String email = "user@example.com";
            String password = "password123";
            User user = savedUser();
            user.setEmail(email);

            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(email, password));
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(any(), eq(user.getId()), any())).thenReturn("access.token");
            when(refreshTokenService.createRefreshToken(user.getId())).thenReturn("refresh.token");
            when(userMapper.toDto(user)).thenReturn(userResponse(user));

            String body = """
                    {
                      "email": "user@example.com",
                      "password": "password123"
                    }
                    """;

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access.token"))
                    .andExpect(jsonPath("$.user.email").value(email))
                    .andExpect(header().exists("Set-Cookie"));
        }

        @Test
        @DisplayName("Invalid password returns 401")
        void login_invalidPassword_returns401() throws Exception {
            String email = "user@example.com";
            String password = "wrong";
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            String body = """
                    {
                      "email": "user@example.com",
                      "password": "wrong"
                    }
                    """;

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());

            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("A non-existent user returns 401 upon login.")
        void login_unknownUser_returns401() throws Exception {
            String email = "nonexistent@example.com";
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("User not found"));

            String body = """
                    {
                      "email": "nonexistent@example.com",
                      "password": "any"
                    }
                    """;

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }
    }
}
