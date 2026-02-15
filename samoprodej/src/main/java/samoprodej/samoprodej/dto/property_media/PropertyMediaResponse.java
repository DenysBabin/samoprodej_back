package samoprodej.samoprodej.dto.property_media;

import samoprodej.samoprodej.enums.MediaType;

import java.time.Instant;
import java.util.UUID;

public record PropertyMediaResponse(
        UUID id,
        UUID propertyId,
        MediaType type,
        String url,
        String previewUrl,
        Integer sortOrder,
        Instant createdAt
) {
}
