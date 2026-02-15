package samoprodej.samoprodej.dto.property_media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import samoprodej.samoprodej.enums.MediaType;

import java.util.UUID;

@Data
public class PropertyMediaDTO {

    private UUID id;
    private UUID propertyId;

    @NotNull
    private MediaType type;

    @NotBlank
    private String url;

    private String previewUrl;
    private Integer sortOrder;
}
