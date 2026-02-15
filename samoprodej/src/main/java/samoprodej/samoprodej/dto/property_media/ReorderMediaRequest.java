package samoprodej.samoprodej.dto.property_media;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderMediaRequest(
        @NotEmpty List<@NotNull UUID> mediaIds
) {
}
