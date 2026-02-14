package samoprodej.samoprodej.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import samoprodej.samoprodej.enums.ErrorCode;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ApiResponse<T> {

    private T data;
    private ApiError error;
    private ApiMeta meta;

    @Data
    @Builder
    public static class ApiError {
        private ErrorCode code;
        private String message;
        private List<ApiErrorField> errors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiErrorField {
        private String field;
        private String message;
    }

    @Data
    @Builder
    public static class ApiMeta {
        private String requestId;
        private Instant timestamp;
        private String path;
    }

    public static <T> ApiResponse<T> success(T data, String requestId, String path) {
        return ApiResponse.<T>builder()
                .data(data)
                .meta(ApiMeta.builder()
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .path(path)
                        .build())
                .build();
    }

    public static ApiResponse<Void> error(ErrorCode code, String message, List<ApiErrorField> errors, String requestId, String path) {
        return ApiResponse.<Void>builder()
                .error(ApiError.builder()
                        .code(code)
                        .message(message)
                        .errors(errors)
                        .build())
                .meta(ApiMeta.builder()
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .path(path)
                        .build())
                .build();
    }
}