package samoprodej.samoprodej.dto.property_media;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import samoprodej.samoprodej.enums.MediaType;

public record CreatePropertyMediaRequest(
        @NotNull MediaType type,
        @NotBlank String url,
        String previewUrl,
        @Min(0) Integer sortOrder
) {
}
