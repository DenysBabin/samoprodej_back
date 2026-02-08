package samoprodej.samoprodej.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import samoprodej.samoprodej.dto.auth.LoginRequest;
import samoprodej.samoprodej.dto.auth.RegisterRequest;
import samoprodej.samoprodej.enums.Role;
import samoprodej.samoprodej.repository.ListingRepository;
import samoprodej.samoprodej.repository.PropertyRepository;
import samoprodej.samoprodej.repository.RefreshTokenRepository;
import samoprodej.samoprodej.repository.UserRepository;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        propertyRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setRole(Role.OWNER);
        request.setPreferredLang("CS");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void shouldLoginAndGetTokens() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("loginuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.TENANT);
        registerRequest.setPreferredLang("EN");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("loginuser@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.user.email").value("loginuser@example.com"))
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldRefreshAccessToken() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("refreshuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.OWNER);
        registerRequest.setPreferredLang("CS");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshTokenCookie = result.getResponse().getCookie("refresh_token");
        String firstAccessToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();

        Thread.sleep(1000);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(resultRefresh -> {
                    String newAccessToken = objectMapper.readTree(resultRefresh.getResponse().getContentAsString()).get("accessToken").asText();
                    if (newAccessToken.equals(firstAccessToken)) {
                        throw new AssertionError("Access token was not rotated!");
                    }
                });
    }

    @Test
    void shouldAccessProtectedEndpointWithToken() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("protected@example.com");
        registerRequest.setPassword("123456");
        registerRequest.setRole(Role.ADMIN);
        registerRequest.setPreferredLang("EN");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("protected@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldFailAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}