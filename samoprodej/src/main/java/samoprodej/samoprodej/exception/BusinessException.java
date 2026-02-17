package samoprodej.samoprodej.exception;

import lombok.Getter;
import samoprodej.samoprodej.enums.ErrorCode;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.BUSINESS_RULE_VIOLATION;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}