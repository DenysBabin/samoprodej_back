package samoprodej.samoprodej.exception;

import jakarta.validation.Valid;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import samoprodej.samoprodej.enums.ErrorCode;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/ex/not-found")
        public void throwNotFound() {
            throw new NotFoundException("Resource not found");
        }

        @GetMapping("/ex/auth")
        public void throwAuth() {
            throw new BadCredentialsException("Wrong password");
        }

        @GetMapping("/ex/business")
        public void throwBusiness() {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email exists");
        }

        @GetMapping("/ex/sql-email")
        public void throwSqlEmail() {
            throw new DataIntegrityViolationException("Duplicate entry 'test@mail.com' for key 'users.uk_email'");
        }

        @GetMapping("/ex/internal")
        public void throwInternal() {
            throw new RuntimeException("Unexpected crash");
        }

        @Data
        static class ValidationDto {
            @jakarta.validation.constraints.NotNull
            private String field;
        }

        @PostMapping("/ex/validation")
        public void throwValidation(@Valid @RequestBody ValidationDto dto) {
            //
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("Client Errors (4xx)")
    class ClientErrors {

        @Test
        @DisplayName("404 Not Found returns correct JSON envelope")
        void handleNotFoundException() throws Exception {
            mockMvc.perform(get("/ex/not-found")
                            .requestAttr("requestId", "req-1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").value("Resource not found"))
                    .andExpect(jsonPath("$.meta.requestId").value("req-1"))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("401 Unauthorized (BadCredentials) returns UNAUTHORIZED code")
        void handleAuthenticationException() throws Exception {
            mockMvc.perform(get("/ex/auth"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.error.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("409 Conflict (Business Exception) returns correct code")
        void handleBusinessException() throws Exception {
            mockMvc.perform(get("/ex/business"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"))
                    .andExpect(jsonPath("$.error.message").value("Email exists"));
        }

        @Test
        @DisplayName("422 Validation Error returns list of fields")
        void handleValidationException() throws Exception {
            mockMvc.perform(post("/ex/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnprocessableEntity()) // 422
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.errors", hasSize(1)))
                    .andExpect(jsonPath("$.error.errors[0].field").value("field"))
                    .andExpect(jsonPath("$.error.errors[0].message").value("must not be null"));
        }

        @Test
        @DisplayName("409 Conflict (DB Unique Constraint) is mapped to specific code")
        void handleDataIntegrityViolation() throws Exception {
            mockMvc.perform(get("/ex/sql-email"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));
        }
    }

    @Nested
    @DisplayName("Server Errors (5xx)")
    class ServerErrors {

        @Test
        @DisplayName("500 Internal Server Error hides details")
        void handleAllExceptions() throws Exception {
            mockMvc.perform(get("/ex/internal")
                            .requestAttr("requestId", "req-500"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.error.message").value("Internal server error"))
                    .andExpect(jsonPath("$.error.details").doesNotExist())
                    .andExpect(jsonPath("$.meta.requestId").value("req-500"));
        }
    }
}