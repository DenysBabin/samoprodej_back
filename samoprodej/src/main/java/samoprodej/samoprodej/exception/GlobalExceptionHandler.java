package samoprodej.samoprodej.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import samoprodej.samoprodej.dto.ApiResponse;
import samoprodej.samoprodej.enums.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String REQUEST_ID_KEY = "requestId";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        log.warn("Business exception: {}", ex.getMessage());
        return buildResponse(ex.getErrorCode(), ex.getMessage(), null, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
            NotFoundException ex, HttpServletRequest request) {
        log.warn("Not found exception: {}", ex.getMessage());
        return buildResponse(ex.getErrorCode(), ex.getMessage(), null, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiResponse.ApiErrorField> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiResponse.ApiErrorField(e.getField(), e.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("Validation failed: {}", errors);
        return buildResponse(ErrorCode.VALIDATION_ERROR, "Validation failed", errors, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String msg = ex.getMostSpecificCause().getMessage();
        log.error("Database conflict: {}", msg);

        ErrorCode code = ErrorCode.CONFLICT;

        if (msg != null) {
            String lowerMsg = msg.toLowerCase();
            if (lowerMsg.contains("email")) {
                code = ErrorCode.EMAIL_ALREADY_EXISTS;
            } else if (lowerMsg.contains("phone")) {
                code = ErrorCode.PHONE_ALREADY_EXISTS;
            } else if (lowerMsg.contains("sort_order") || lowerMsg.contains("sortorder")) {
                code = ErrorCode.PROPERTY_MEDIA_SORT_ORDER_TAKEN;
            }
        }

        return buildResponse(code, code.getDefaultMessage(), null, request);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse(ErrorCode.UNAUTHORIZED, "Invalid email or password", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return buildResponse(ErrorCode.INTERNAL_ERROR, "Internal server error", null, request);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            ErrorCode code, String message, List<ApiResponse.ApiErrorField> errors, HttpServletRequest request) {
        String requestId = (String) request.getAttribute(REQUEST_ID_KEY);
        if (requestId == null) requestId = "unknown";

        ApiResponse<Void> response = ApiResponse.error(
                code,
                message,
                errors,
                requestId,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, code.getHttpStatus());
    }
}