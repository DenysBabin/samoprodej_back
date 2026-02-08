package samoprodej.samoprodej.dto.propertymedia;

import jakarta.validation.constraints.Min;

public record UpdatePropertyMediaRequest(
        String previewUrl,
        @Min(0) Integer sortOrder
) {
}
