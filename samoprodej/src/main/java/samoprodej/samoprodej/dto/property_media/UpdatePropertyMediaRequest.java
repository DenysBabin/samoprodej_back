package samoprodej.samoprodej.dto.property_media;

import jakarta.validation.constraints.Min;

public record UpdatePropertyMediaRequest(
        String previewUrl,
        @Min(0) Integer sortOrder
) {
}
