package samoprodej.samoprodej.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed"),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violation"),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT, "Resource conflict"),

    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication failed"),

    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "Email already registered"),
    PHONE_ALREADY_EXISTS("PHONE_ALREADY_EXISTS", HttpStatus.CONFLICT, "Phone number already registered"),
    PROPERTY_MEDIA_SORT_ORDER_TAKEN("PROPERTY_MEDIA_SORT_ORDER_TAKEN", HttpStatus.CONFLICT, "Sort order already taken");

    @JsonValue
    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}