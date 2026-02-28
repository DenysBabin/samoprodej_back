package samoprodej.samoprodej.dto.application;

import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @Size(max = 1000, message = "Message must not exceed 1000 characters")
        String message
) {
}